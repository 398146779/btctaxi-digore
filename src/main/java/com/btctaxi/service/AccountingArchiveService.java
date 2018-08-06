package com.btctaxi.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import genesis.accounting.config.State;
import genesis.accounting.config.WalletConfig;
import genesis.accounting.controller.support.ErrorCodeEnum;
import genesis.accounting.controller.support.ServiceError;
import genesis.accounting.dao.AccountingArchiveRepository;
import genesis.accounting.dao.AccountingCurrencyRepository;
import genesis.accounting.dao.AccountingDepositRepository;
import genesis.accounting.domain.AccountingArchive;
import genesis.accounting.domain.AccountingCurrency;
import genesis.accounting.domain.AccountingDeposit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: guangtou
 * Date: 2018/6/27 14:45
 */
@Service
@Slf4j
public class AccountingArchiveService {
    @Autowired
    private AccountingArchiveRepository accountingArchiveRepository;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AccountingCurrencyRepository accountingCurrencyRepository;
    @Autowired
    private AccountingDepositRepository accountingDepositRepository;

    @Autowired
    private WalletConfig walletConfig;

    @Autowired
    HttpService httpService;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public static String ETH = "ETH";
    public static String BTC = "BTC";
    public static String OMNI = "OMNI";

//    private String walletBalanceUrl = getWalletBalanceUrl();

    private String getWalletBalanceUrl() {
        return walletConfig.getQueryUrl() + WALLET_BALANCE_URI;
    }

    private static String WALLET_BALANCE_URI = "balance?address=%s&chain=%s&currency=%s";

    private static ArrayList<Integer> PADDING_STATE_LIST = Lists.newArrayList(State.WITHDRAW_CREATED, State.WITHDRAW_REVIEWED1, State.WITHDRAW_REVIEWED2, State.WITHDRAW_BROADCASTED, State.WITHDRAW_SIGNED);

    /**
     * 生成摘要, sha256(key + txtype + withdraw_id + currency)
     */
    public String getIdSha256HexString(AccountingArchive archive) {
        String wallet_tx_id = archive.getWalletTxId();

        if (StringUtils.isNotEmpty(wallet_tx_id)) {
            return wallet_tx_id;
        }

        Long id = archive.getId();
        String data = new StringBuilder(walletConfig.getKey())
                .append(archive.getTxType())
                .append(id)
                .append(archive.getCurrencyName())
                .toString();

        String hex = DigestUtils.sha256Hex(data);
        log.debug("生成钱包请求: withdraw.getId()={}, sha256Hex={}", id, hex);
        return hex;
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean review1(Long id,
                           Integer state,
                           Long reviewerId,
                           Long reviewerNonce,
                           String reviewerSignature,
                           String address,
                           String changeAddress) {

        AccountingArchive archive = accountingArchiveRepository.findOne(id);

        if (archive == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.DATA_NOT_FOND);
        }

        Integer stateInDb = archive.getState();

        if (!Lists.newArrayList(State.WITHDRAW_CREATED, State.WITHDRAW_ERROR_RETRY).contains(stateInDb)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!ReviewService.review1StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }


        //  "状态不可逆, Irreversible state !");
        String hex = getIdSha256HexString(archive);
        archive.setWalletTxId(hex);

        BigDecimal amount = archive.getAmount();
        if (state == State.WITHDRAW_REFUSE1) {
            //一审拒绝
            log.info("review1 一审拒绝中, hex={}, id={}", archive.getWalletTxId(), archive.getId());

//            Balance balance = this.findBalanceByUserIdAndCurrencyName(userId, archive.getCurrency_name());
//            BigDecimal oldWithdrawing = balance.getWithdrawing();
//            calCancelAmount(archive, balance);
//            this.saveBalanceReview1(balance, oldWithdrawing);

            archive.setReview1Time(new Timestamp(System.currentTimeMillis()));
            archive.setReviewer1Id(reviewerId);


            archive.setState(State.WITHDRAW_REFUSE1);
            accountingArchiveRepository.saveAndFlush(archive);

            log.info("review1 一审拒绝完成, hex={}, id={}", archive.getWalletTxId(), archive.getId());

            return true;
        }

        log.info("review1 一审通过中, hex={}, id={}", archive.getWalletTxId(), archive.getId());
        //一审 通过
        if (state != State.WITHDRAW_REVIEWED1) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (reviewerNonce == null || reviewerSignature == null)
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        if (address == null)
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);


        //判断矿工费是否充足
//        Pair<String, String> pair = getChainNameCurrencyNamePair(archive.getChainName(), archive.getCurrencyName());
//        BigDecimal walletBalance = getWalletBalance(getWalletBalanceUrl(), address, pair.getLeft(), pair.getRight());
//        if (walletBalance.compareTo(BigDecimal.ZERO) < 1) {
//            ServiceError.ofAndThrow(ErrorCodeEnum.GAS_NOT_ENOUGH);
//        }

        //判断是否有在途 BTC 一审 todo


        archive.setReviewer1Nonce(reviewerNonce);
        archive.setReviewer1Signature(reviewerSignature);
        archive.setChangeAddress(changeAddress == null ? address : changeAddress);

        archive.setReviewer1Id(reviewerId);
        if (archive.getTxType() == 2) {
            archive.setFromAddress(address);
        } else {
            archive.setToAddress(address);
        }
        archive.setReview1Time(new Timestamp(System.currentTimeMillis()));


        try {
            JSONObject post = httpService.post(ReviewService.CREATE1URL,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", archive.getChainName(), //chain
                    "currency", archive.getCurrencyName(), //currency
                    "txtype", archive.getTxType(), //txtype
                    "from_address", archive.getFromAddress(), //from_address
                    "to_address", archive.getToAddress(), //to_address
                    "change_address", archive.getChangeAddress(), //change_address
                    "memo", archive.getMemo(), //memo
                    "amount", amount.stripTrailingZeros().toPlainString(), //amount
                    "reviewer_id1", reviewerId, //reviewer_id1
                    "reviewer_nonce1", reviewerNonce, //reviewer_nonce1
                    "reviewer_signature1", reviewerSignature //reviewer_signature1
            );

        /*发起转账: POST /transaction/review1
        返回值:  id chain currency raw_transaction state create_time*/
            //walletState /-2已取消 -1未审核 0已审核未签名 1已签名 2广播已发出 3广播失败 4广播完成/
            log.info("调用钱包 review1 返回: sha256Hex={}, responseValue={}", hex, post);

            String rawTransaction = post.getString("raw_transaction");
            if (rawTransaction == null) {
                log.error("钱包 review1 返回值无 rawTX 值, {}", hex);
                ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
            }

            archive.setState(state);
            archive.setRawTx(rawTransaction);

            log.info("sha256Hex={}, saved db ", hex);
            accountingArchiveRepository.saveAndFlush(archive);
            log.info("review1 一审通过 完成, hex={}, id={}", archive.getWalletTxId(), archive.getId());

            return true;
        } catch (Exception e) {
            log.error(String.format("调用钱包 review1 失败: 256Hex:%s", hex), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_REVIEW1_FAILED, e);

            return false;
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean review2(Long id,
                           Integer state,
                           Long reviewerId,
                           Long reviewerNonce,
                           String reviewerSignature) {

        AccountingArchive archive = accountingArchiveRepository.findOne(id);
        Integer stateInDb = archive.getState();

        if (stateInDb != State.WITHDRAW_REVIEWED1) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        //先调用一审接口, please call review1 interface !
        if (archive.getReviewer1Id() == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PRE_CALL_REVIEW1);
        }


        if (state == State.WITHDRAW_REFUSE2) {
            //调用钱包 walletCancelService 接口
            log.info("review1 2审拒绝中, hex={}, id={}", archive.getWalletTxId(), archive.getId());
            if (ReviewService.review2StateList.contains(archive.getState()) || ReviewService.finalStateList.contains(archive.getState())) {
                ServiceError.ofAndThrow(ErrorCodeEnum.REVIEW2_NOT_CANCEL);
            }
            walletCancel(archive);

            String hex = getIdSha256HexString(archive);


//            this.saveBalanceReview1(balance, oldWithdrawing);
//            this.saveWithdrawReview2Refuse(archive);
            accountingArchiveRepository.saveAndFlush(archive);
            log.info("review1 2审拒绝完成, hex={}, id={}", archive.getWalletTxId(), archive.getId());
            return true;
        }


        log.info("review1 2审通过中, hex={}, id={}", archive.getWalletTxId(), archive.getId());

//        if (state == REVIEW2.getValue()) {
        if (reviewerNonce == null || reviewerSignature == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        }

        archive.setReviewer2Nonce(reviewerNonce);
        archive.setReviewer2Signature(reviewerSignature);


        //转账2审签名 POST /transaction/review2
        //参数data id(sha256(key+txtype+internaltxid+currency), deduplicate)chain currency reviewer_id2 reviewer_nonce2 reviewer_signature2
        //返回值 id chain currency raw_transaction state review_time

        String hex = getIdSha256HexString(archive);
        JSONObject post = httpService.post(ReviewService.CREATE2URL,
                "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                "chain", archive.getChainName(), //chain
                "currency", archive.getCurrencyName(), //currency
                "reviewer_id2", reviewerId,//reviewer_id2
                "reviewer_nonce2", reviewerNonce,//reviewer_nonce2
                "reviewer_signature2", reviewerSignature//reviewer_signature2
        );


        archive.setState(state);
        archive.setReviewer2Id(reviewerId);
        archive.setReview2Time(new Timestamp(System.currentTimeMillis()));
        String rawTransaction = post.getString("raw_transaction");

        if (rawTransaction == null) {
            log.error("钱包返回 rawTransaction 异常 rawTransaction is null");
            //钱包返回异常
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        }


        accountingArchiveRepository.saveAndFlush(archive);
//        this.saveWithdrawReview2(archive);
        log.info("review2 2审通过 完成, hex={}, id={}", archive.getWalletTxId(), archive.getId());
        return true;
    }


    public void walletCancel(AccountingArchive archive) {
        log.info("walletCancel WalletTxId=hex={}, id={}", archive.getWalletTxId(), archive.getId());

        String hex = getIdSha256HexString(archive);
        archive.setWalletTxId(hex);
        try {
            JSONObject post = httpService.post(ReviewService.CANCELURL,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", archive.getChainName(), //chain
                    "currency", archive.getCurrencyName()//currency
            );


            String stateInWallet = post.getString("state");
            if (!"-2".equals(stateInWallet)) {
                //  "钱包取消失败");
                log.error("钱包取消失败, hex:{}", hex);
                ServiceError.ofAndThrow(ErrorCodeEnum.CANCEL_FAILED);
            }
            archive.setRawTx(null);
            archive.setState(State.WITHDRAW_REFUSE2);

//            BigDecimal oldWithdrawing = balance.getWithdrawing();
//            calCancelAmount(archive, balance);
//            this.saveBalanceReview1(balance, oldWithdrawing);
//            this.saveWithdrawReview2Refuse(archive);
        } catch (Exception e) {
            log.error(String.format("调用钱包 cancel 失败: 256Hex:%s", hex), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_CANCEL_FAILED, e);
        }


    }


    /**
     * 校验转账状态
     * 转账成功 job 爬取钱包
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkWithdrawStatus() {

        //需要同步的状态的订单 1,2审
        Page<AccountingArchive> archivs = accountingArchiveRepository.findAllByStateInOrderByCreateTimeAsc(new PageRequest(0, 1000), ReviewService.jobStateList);

        List<AccountingArchive> archiveList = archivs.getContent();

        log.info("transferToSuccess begin , list.size={}", archiveList.size());
        //处理原则: 能修复即修复, 否则标记为提币失败
        archiveList.forEach(archive -> {
            log.info("正在刷新状态 archive in DB  id={}: , hex={}, archive.state={} ", archive.getId(), archive.getWalletTxId(), archive.getState());

            Integer stateInDb = archive.getState();
            Long id = archive.getId();
            try {
                JSONObject walletTransaction = getWalletTransaction(archive);
                //state /-2已取消 -1未审核 0已审核未签名 1已签名 2广播已发出 3广播失败 4广播完成/
                int stateInWallet = walletTransaction.getIntValue("state");
                if (stateInDb == State.WITHDRAW_REVIEWED1) {

                    String rawTransaction = StringUtils.trimToNull(walletTransaction.getString("rawTransaction"));
                    if (archive.getRawTx() == null && rawTransaction != null) {
                        archive.setRawTx(rawTransaction);
                        accountingArchiveRepository.saveAndFlush(archive);
//                        this.saveWithdrawRawtx(archive);
                    }
                } else if (stateInDb == State.WITHDRAW_REVIEWED2) {
                    //state /-2已取消 -1未审核 0已审核未签名 1已签名 2广播已发出 3广播失败 4广播完成/

                    String chainTxid = walletTransaction.getString("chain_txid");
                    int chainConfirm = walletTransaction.getIntValue("chain_confirm");
                    if (stateInWallet == 2) {
                        archive.setTxid(chainTxid);
                        archive.setConfirm(chainConfirm);
                        archive.setState(State.WITHDRAW_REVIEWED2);
                        accountingArchiveRepository.saveAndFlush(archive);
                    } else if (stateInWallet == 4) {
                        archive.setState(State.SUCCESS);
                        archive.setTxid(chainTxid);
                        archive.setConfirm(chainConfirm);

//                        BigDecimal amount = archive.getAmount();
//                        BigDecimal fee = archive.getMinerAmount();
//                        Balance balance = this.findBalanceByUserIdAndCurrencyName(archive.getUser_id(), archive.getCurrency_name());
//                        accountingArchiveRepository.saveAndFlush(archive);
//                        BigDecimal oldWithdrawing = balance.getWithdrawing();
//                        balance.setWithdrawing(oldWithdrawing.subtract(amount).subtract(fee));

                        accountingArchiveRepository.saveAndFlush(archive);
                    } else if (stateInWallet == 3) {//打回重申
                        archive.setState(State.WITHDRAW_ERROR_RETRY);
                        archive.setTxid(chainTxid);
                        archive.setConfirm(chainConfirm);
                        archive.setFailReason(walletTransaction.getString("broadcast_fail_reason"));

                        accountingArchiveRepository.saveAndFlush(archive);

                    }
                }
            } catch (Exception e) {
                log.error(String.format("钱包处理失败, hex=%s", archive.getWalletTxId()), e);
            }

        });
    }


    public JSONObject getWalletTransaction(AccountingArchive archive) {
        String hex = getIdSha256HexString(archive);
        String chainName = archive.getChainName();
        String currencyName = archive.getCurrencyName();
        return getWalletTransaction(hex, chainName, currencyName);
    }

    public JSONObject getWalletTransaction(String hex, String chainName, String currencyName) {
        try {
            JSONObject post = httpService.post(ReviewService.TRANSACTION,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", chainName, //chain
                    "currency", currencyName //currency
            );
            return post;

        } catch (Exception e) {
            log.error(String.format("getWalletTransaction error: hex=%s, chain=%s, currency=%s", hex, chainName, currencyName), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_TRANSACTION_FAILED, e);
            return null;
        }
    }


    /**
     * //归集： 列表 （一审、二审、重审， 发送中，异常）； 一审接口，二审接口
     * //矿工费： 列表 （一审、二审、重审， 发送中，异常）； 一审接口，二审接口
     *
     * @param chainName
     * @param gasCurrencyName
     * @param preRunTime
     */
    @Transactional(rollbackFor = Throwable.class)
    public void create(String chainName, String gasCurrencyName, Date preRunTime) {

        if (StringUtils.isEmpty(chainName) || preRunTime == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR);
        }

        byte depositSuccessState = 1;

        log.info("创建归集: chainName={},preRunTime={}", chainName, preRunTime);

        //查询 ETH 链 的币种
        //查询入金记录 ETH 链 & ETH 币 的 钱包地址 , 去重
        List<AccountingDeposit> depositList = accountingDepositRepository.findAllByStateAndChainNameAndCreateTimeAfter(depositSuccessState, chainName, preRunTime);

        List<AccountingCurrency> currencyList = accountingCurrencyRepository.findAll();
        LinkedHashMultimap<String, String> multimap = LinkedHashMultimap.create();
        depositList.forEach(tmp -> multimap.put(tmp.getToAddress(), tmp.getCurrencyName()));

        //保证 ETH&ETH 务必出现
        multimap.keySet().forEach(s -> multimap.put(s, gasCurrencyName));

        for (Map.Entry<String, String> kvEntry : multimap.entries()) {

                String address = kvEntry.getKey();
                String currencyName = kvEntry.getValue();
                Pair<String, String> pair = getChainNameCurrencyNamePair(chainName, currencyName);
                String chainNameTmp = pair.getLeft();
                String currencyNameTmp = pair.getRight();
            try {

                //查询所有 钱包中 链&币 的资金
                BigDecimal balanceAmountInWallet = getWalletBalance(getWalletBalanceUrl(), address, chainName, currencyName);

                //padding 矿工费
                List<AccountingArchive> gasList = accountingArchiveRepository.findAllPaddingArchiveByToAddress(PADDING_STATE_LIST, address, chainNameTmp, currencyNameTmp);
                if (!CollectionUtils.isEmpty(gasList)) {
                    log.info("存在 {} 笔在途旷工费 gasList size=, address={}", gasList.size(), address);
                    continue;
                }

                //padding 归集
                List<AccountingArchive> paddingArchive = accountingArchiveRepository.findAllPaddingArchiveByFromAddress(PADDING_STATE_LIST, address, chainNameTmp, currencyNameTmp);
                if (!CollectionUtils.isEmpty(paddingArchive)) {
                    log.info("存在 {} 笔在途归集 paddingArchive  address={}", paddingArchive.size(), address);
                    continue;
                }


                Timestamp time = new Timestamp(System.currentTimeMillis());
                AccountingArchive archive = AccountingArchive.builder()
                        .state(State.WITHDRAW_CREATED)
                        .chainName(chainNameTmp)
                        .currencyName(currencyNameTmp)
                        .fee(BigDecimal.ZERO)
                        .confirm(0)
                        .createTime(time)
                        .updateTime(time)
                        .build();

                AccountingCurrency currency = currencyList.stream()
                        .filter(a -> chainNameTmp.equals(a.getChainName()) && currencyNameTmp.equals(a.getCurrencyName()))
                        .findFirst().get();


                Optional<BigDecimal> sumBalance = currencyList.stream()
                        .filter(a -> chainName.equals(a.getChainName()) && !Lists.newArrayList(BTC, ETH).contains(a.getCurrencyName()))
                        .map(c -> getWalletBalance(getWalletBalanceUrl(), address, chainName, c.getCurrencyName()))
                        .reduce(BigDecimal::add);


                BigDecimal minFee = currency.getMinerMinAmount();//getMinerMinAmount(chainNameTmp, currencyNameTmp, currencyList);//矿工费
                BigDecimal fee = currency.getMinerAmount();// getMinerAmount(chainNameTmp, currencyNameTmp, currencyList);
                BigDecimal archiveAmount = currency.getArchiveAmount();// getMinWithdrawAmount(chainNameTmp, currencyNameTmp, currencyList);


                //创建冲矿工费:
                if (gasCurrencyName.equals(currencyName)
                        && balanceAmountInWallet.compareTo(minFee) < 0
                        && sumBalance.orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {

                    archive.setToAddress(address);
                    archive.setAmount(fee);
                    archive.setTxType(2);
                    log.info("创建矿工费 archive save type = 2 , balanceAmountInWallet={} < minFee={}, ", balanceAmountInWallet, minFee);
                    accountingArchiveRepository.saveAndFlush(archive);
                    continue;
                }

                BigDecimal subtract = balanceAmountInWallet.subtract(fee);
                if (balanceAmountInWallet.compareTo(archiveAmount) < 0 || subtract.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("  余额不足不创建归集 balanceAmountInWallet is not enough , balanceAmountInWallet={}, archiveAmount={}, address={}", balanceAmountInWallet, archiveAmount, address);
                    continue;
                }

                if (BTC.equals(chainNameTmp) && BTC.equals(currencyNameTmp)){
                    log.info("  BTC 不创建归集, address={}", address);
                    continue;
                }

                //if !ETH balance < fee , 剔除掉 充值中, 创建归集中的的币 other 创建归集
                archive.setFromAddress(address);
                archive.setAmount(balanceAmountInWallet);
                if (gasCurrencyName.equals(currencyName)) {
                    archive.setAmount(subtract);
                }
                archive.setTxType(1);
                log.info("创建归集 archive save type = 1  archive={}, ", archive);

                accountingArchiveRepository.saveAndFlush(archive);
            } catch (Exception e) {
                log.error(String.format("create archive error , kvEntry = %s", kvEntry), e);
            }

        }
        log.info("创建归集 完成! size={}", depositList.size());
    }

//    private BigDecimal getMinerMinAmount(String chainName, String currencyName, List<AccountingCurrency> accountingCurrencies) {
//        AccountingCurrency currency = accountingCurrencies.stream().filter(a -> chainName.equals(a.getChainName()) && currencyName.equals(a.getCurrencyName())).findFirst().get();
//        return currency.getMinerMinAmount();
//    }
//
//
//    private BigDecimal getMinerAmount(String chainName, String currencyName, List<AccountingCurrency> accountingCurrencies) {
//        AccountingCurrency currency = accountingCurrencies.stream().filter(a -> chainName.equals(a.getChainName()) && currencyName.equals(a.getCurrencyName())).findFirst().get();
//        return currency.getMinerAmount();
//    }
//
//    private BigDecimal getMinWithdrawAmount(String chainName, String currencyName, List<AccountingCurrency> accountingCurrencies) {
//        AccountingCurrency currency = accountingCurrencies.stream().filter(a -> chainName.equals(a.getChainName()) && currencyName.equals(a.getCurrencyName())).findFirst().get();
//        return currency.getMinWithdrawAmount();
//    }

    private BigDecimal getWalletBalance(String url, String address, String chainName, String currencyName) {
        BigDecimal balance = restTemplate.getForObject(String.format(url, address, chainName, currencyName), BigDecimal.class);
        log.info("      钱包中资金 balanceAmountInWallet={}, address={}, chainNameTmp={}, currencyNameTmp={} ", balance, address, chainName, currencyName);

        return balance;
    }

    /**
     * 需要特殊处理的连
     *
     * @param chainName
     * @param currencyName
     * @return
     */
    private Pair<String, String> getChainNameCurrencyNamePair(String chainName, String currencyName) {
        Pair<String, String> pair = Pair.of(chainName, currencyName);
        if (OMNI.equals(chainName) && BTC.equals(currencyName)) {
            pair = Pair.of(BTC, BTC);
        }
        return pair;
    }

    private Pair<String, String> getGasPair(String chainName) {
        Pair<String, String> pair = Pair.of(chainName, chainName);
        if (OMNI.equals(chainName) || BTC.equals(chainName)) {
            pair = Pair.of(OMNI, BTC);
        } else if (ETH.equals(chainName)) {
            return Pair.of(ETH, ETH);
        }
        return pair;
    }

}
