package com.btctaxi.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import genesis.accounting.config.State;
import genesis.accounting.config.WalletConfig;
import genesis.accounting.controller.support.ErrorCodeEnum;
import genesis.accounting.controller.support.ServiceError;
import genesis.accounting.domain.Balance;
import genesis.accounting.domain.Withdraw;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private Data data;
    private WalletConfig walletConfig;
    @Autowired
    private HttpService http;
    @Autowired
    private WithdrawService withdrawService;
    @Autowired
    private CurrencyService currencyService;


    public ReviewService(Data data, WalletConfig walletConfig, HttpService http) {
        this.data = data;
        this.walletConfig = walletConfig;
        this.http = http;
    }

//    @Transactional(readOnly = true)
//    public List<DataMap> listRequest() {
//        String sql = "SELECT id, user_id, chain_name, currency_name, to_address, amount, fee , create_time FROM accounting_withdraw WHERE state = 0";
//        List<DataMap> withdraws = data.query(sql);
//        for (DataMap withdraw : withdraws) {
//            long id = withdraw.getLong("id");
//            String currencyName = withdraw.getString("currency_name");
//            String walletTxId = DigestUtils.sha256Hex(walletConfig.getKey() + "0" + id + currencyName);
//            withdraw.put("wallet_tx_id", walletTxId);
//        }
//        return withdraws;
//    }
//
//    @Transactional(readOnly = true)
//    public List<DataMap> listReview1() {
//        String sql = "SELECT id, wallet_tx_id, user_id, chain_name, currency_name, from_address, to_address, change_address, memo, amount, fee, create_time, reviewer1_id, reviewer1_nonce, reviewer1_signature, review1_time, raw_tx FROM accounting_withdraw WHERE state = 1";
//        List<DataMap> withdraws = data.query(sql);
//        return withdraws;
//    }
//
//    @Transactional(readOnly = true)
//    public List<DataMap> listReview2() {
//        String sql = "SELECT id, wallet_tx_id, user_id, chain_name, currency_name, from_address, to_address, change_address, amount, fee, state, create_time, update_time, reviewer1_id, review1_time, raw_tx, reviewer2_id, review2_time FROM accounting_withdraw WHERE state = 1";
//        List<DataMap> withdraws = data.query(sql);
//        return withdraws;
//    }
//
//    @Transactional(readOnly = true)
//    public DataMap query(long id) {
//        String sql = "SELECT id, user_id, chain_name, currency_name, from_address, to_address, change_address, amount, fee, state, create_time, update_time, reviewer1_id, review1_time, raw_tx, reviewer2_id, review2_time FROM accounting_withdraw WHERE id = ?";
//        DataMap withdraw = data.queryOne(sql, id);
//        return withdraw;
//    }
//
//
//    @Transactional(rollbackFor = Throwable.class)
//    public void review1(long id, long userId, long reviewer1Id, long reviewer1Nonce, String reviewer1Signature, String fromAddress) {
//        String sql = "SELECT chain_name, currency_name, to_address, memo, amount FROM accounting_withdraw WHERE id = ? AND user_id = ? AND state = ?";
//        DataMap withdraw = data.queryOne(sql, id, userId, State.WITHDRAW_CREATED);
//        if (withdraw == null)
//            throw new RuntimeException("invalid entry");
//        String chainName = withdraw.getString("chain_name");
//        String currencyName = withdraw.getString("currency_name");
//        String toAddress = withdraw.getString("to_address");
//        String memo = withdraw.getString("memo");
//        BigDecimal amount = withdraw.getBig("amount");
//
//        //TODO chain?
//        //TODO memo
//        String walletTxId = DigestUtils.sha256Hex(walletConfig.getKey() + "0" + id + currencyName);
//        JSONObject json = http.post("/transaction/create1", "id", walletTxId, "chain", chainName, "currency", currencyName, "txtype", "0", "from_address", fromAddress, "to_address", toAddress, "change_address", fromAddress, "amount", amount.toPlainString(), "reviewer_id1", reviewer1Id, "reviewer_nonce1", reviewer1Nonce, "reviewer_signature1", reviewer1Signature);
//        String rawTx = json.getString("raw_transaction");
//        if (rawTx == null)
//            throw new RuntimeException("create1 error");
//
//        sql = "UPDATE accounting_withdraw SET wallet_tx_id = ?, state = ?, from_address = ?, change_address = ?, reviewer1_id = ?, reviewer1_nonce = ?, reviewer1_signature = ?, review1_time = NOW(), raw_tx = ? WHERE id = ? AND user_id = ? AND state = ?";
//        int rowN = data.update(sql, walletTxId, State.WITHDRAW_REVIEWED1, fromAddress, fromAddress, reviewer1Id, reviewer1Nonce, reviewer1Signature, rawTx, id, userId, State.WITHDRAW_CREATED);
//        if (rowN < 1)
//            throw new RuntimeException("concurrent error");
//    }
//
//    @Transactional(rollbackFor = Throwable.class)
//    public void review2(long id, long userId, long reviewer2Id, long reviewer2Nonce, String reviewer2Signature) {
//        String sql = "SELECT chain_name, currency_name FROM accounting_withdraw WHERE id = ? AND user_id = ? AND state = ?";
//        DataMap withdraw = data.queryOne(sql, id, userId, State.WITHDRAW_REVIEWED1);
//        if (withdraw == null)
//            throw new RuntimeException("invalid entry");
//        String chainName = withdraw.getString("chain_name");
//        String currencyName = withdraw.getString("currency_name");
//
//        String walletTxId = DigestUtils.sha256Hex(walletConfig.getKey() + "0" + id + currencyName);
//        JSONObject json = http.post("/transaction/create2", "id", walletTxId, "chain", chainName, "currency", currencyName, "reviewer_id2", reviewer2Id, "reviewer_nonce2", reviewer2Nonce, "reviewer_signature2", reviewer2Signature);
//        String rawTx = json.getString("raw_transaction");
//        if (rawTx == null)
//            throw new RuntimeException("create2 error");
//
//        sql = "UPDATE accounting_withdraw SET state = ?, reviewer2_id = ?, reviewer2_nonce = ?, reviewer2_signature = ?, review2_time = NOW() WHERE id = ? AND user_id = ? AND state = ?";
//        int rowN = data.update(sql, State.WITHDRAW_REVIEWED2, reviewer2Id, reviewer2Nonce, reviewer2Signature, id, userId, State.WITHDRAW_REVIEWED1);
//        if (rowN < 1)
//            throw new RuntimeException("concurrent error");
//    }
//
//    @Transactional(rollbackFor = Throwable.class)
//    public void refuse(long id, long userId) {
//        String sql = "SELECT chain_name, currency_name, amount, fee, state FROM accounting_withdraw WHERE id = ? AND user_id = ?";
//        DataMap withdraw = data.queryOne(sql, id, userId);
//        if (withdraw == null)
//            throw new RuntimeException("invalid entry");
//        int state = withdraw.getInt("state");
//
//        if (state != State.WITHDRAW_CREATED && state != State.WITHDRAW_REVIEWED1)
//            throw new RuntimeException("invalid state");
//
//        String chainName = withdraw.getString("chain_name");
//        String currencyName = withdraw.getString("currency_name");
//        BigDecimal amount = withdraw.getBig("amount");
//        BigDecimal fee = withdraw.getBig("fee");
//
//        if (state == State.WITHDRAW_REVIEWED1) {
//            String walletTxId = DigestUtils.sha256Hex(walletConfig.getKey() + "0" + id + currencyName);
//            JSONObject json = http.post("/transaction/cancel", "id", walletTxId, "chain", chainName, "currency", currencyName);
//            if (!json.containsKey("id"))
//                throw new RuntimeException("cancel error");
//        }
//
//        int newState = state == State.WITHDRAW_CREATED ? State.WITHDRAW_REFUSE1 : State.WITHDRAW_REFUSE2;
//
//        BigDecimal total = amount.add(fee);
//
//        sql = "UPDATE accounting_balance SET available = available + ?, withdrawing = withdrawing - ? WHERE user_id = ? AND currency_name = ?";
//        data.update(sql, total, total, userId, currencyName);
//
//        sql = "UPDATE accounting_withdraw SET state = ? WHERE id = ? AND user_id = ? AND state = ?";
//        int rowN = data.update(sql, newState, id, userId, state);
//        if (rowN < 1)
//            throw new RuntimeException("concurrent error");
//    }


    //-------------------- fhtodo

    @Transactional(readOnly = true)
    public List<DataMap> listAddress(String chainName, String currencyName) {
        String sql = "SELECT address FROM accounting_internal_address WHERE chain_name = ?";
        List<DataMap> addresses = data.query(sql, chainName);
        return addresses;
    }

    public static String CREATE1URL = "/transaction/create1";
    public static String CREATE2URL = "/transaction/create2";
    public static String CANCELURL = "/transaction/cancel";
    public static String LIST = "/transaction/list";
    public static String TRANSACTION = "/transaction";

    private static final Integer txType = 0;

    @Autowired
    private HttpService httpService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public static final ArrayList<Integer> jobStateList = Lists.newArrayList(
//            State.WITHDRAW_CREATED,
//            State.WITHDRAW_REFUSE1,
            State.WITHDRAW_REVIEWED1,
            State.WITHDRAW_REVIEWED2
    );
    public static final ArrayList<Integer> review2StateList = Lists.newArrayList(
            State.WITHDRAW_REVIEWED2,
            State.WITHDRAW_REFUSE2
    );

    public static final ArrayList<Integer> review1StateList = Lists.newArrayList(
            State.WITHDRAW_REVIEWED1,
            State.WITHDRAW_REFUSE1
    );

    //终态
    public static final ArrayList<Integer> finalStateList = Lists.newArrayList(
            State.SUCCESS,
            State.WITHDRAW_CANCELED,
            State.WITHDRAW_REFUSE1,
            State.WITHDRAW_REFUSE2
    );

    public static final ArrayList<Integer> oaAllowStateList = Lists.newArrayList(
            State.WITHDRAW_REVIEWED1,
            State.WITHDRAW_REVIEWED2,
            State.WITHDRAW_REFUSE1,
            State.WITHDRAW_REFUSE2
    );

    /**
     * 生成摘要, sha256(key + txtype + withdraw_id + currency)
     *
     * @param withdraw
     * @return
     */
    private String getIdSha256HexString(Withdraw withdraw) {
        String wallet_tx_id = withdraw.getWallet_tx_id();

        if (StringUtils.isNotEmpty(wallet_tx_id)) {
            return wallet_tx_id;
        }

        Long id = withdraw.getId();
        String data = new StringBuilder(walletConfig.getKey())
                .append(txType)
                .append(id)
                .append(withdraw.getCurrency_name())
                .toString();

        String hex = DigestUtils.sha256Hex(data);
        log.debug("生成钱包请求: withdraw.getId()={}, sha256Hex={}", id, hex);
        return hex;
    }

    /**
     * 计算拒绝提币资金
     *
     * @param withdraw
     * @param balance
     */
    private void calCancelAmount(Withdraw withdraw, Balance balance) {
        BigDecimal amount = withdraw.getAmount();
        BigDecimal available = balance.getAvailable();
        BigDecimal withdrawing = balance.getWithdrawing();
        BigDecimal fee = withdraw.getFee();

        //set available
        balance.setAvailable(available.add(amount).add(fee));
        //set withdraw
        balance.setWithdrawing(withdrawing.subtract(amount).subtract(fee));
//        this.saveBalanceReview1(balance, withdrawing);
    }

    private String allCols = "  id, state, wallet_tx_id, user_id, chain_name, currency_name, from_address, to_address, change_address, memo, amount, fee, txid, confirm, create_time, update_time, reviewer1_id, reviewer1_nonce, review1_time, raw_tx, reviewer2_id, reviewer2_nonce, review2_time ,reviewer2_signature,reviewer1_signature, fail_reason";

    private List<Withdraw> findAllWithdrawByUserIdAndStateIn(List<Integer> states, Long userId) {
        String sql = "select " + allCols + " from accounting_withdraw where state in (" + StringUtils.join(states, ",") + ") and user_id = ? ";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(Withdraw.class));
    }

    private List<Withdraw> findAllByStateIn(List<Integer> states) {
        String sql = "select " + allCols + " from accounting_withdraw where state in (" + StringUtils.join(states, ",") + ") order by create_time ";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Withdraw.class));
    }

    private Withdraw findAllWithdrawByIdAndUserId(Long id, Long userId) {
        String sql = "select " + allCols + " from accounting_withdraw where id = ? and user_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id, userId}, new BeanPropertyRowMapper<>(Withdraw.class));
    }

    private Withdraw findWithdrawById(Long id) {
        String sql = "select " + allCols + " from accounting_withdraw where id = ? ";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Withdraw.class));
    }

//    private List<Withdraw> findByIdAndUserId(Integer id, Integer userId) {
//        String sql = "select * from accounting_withdraw where id = ? and user_id = ?";
//        return jdbcTemplate.query(sql, new Object[]{id, userId}, new BeanPropertyRowMapper<>(Withdraw.class));
//    }

    private Balance findBalanceByUserIdAndCurrencyName(Long userId, String currencyName) {
        String sql = "select user_id, currency_name, available, ordering, withdrawing, locking from accounting_balance where user_id = ? and currency_name = ?  ";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId, currencyName}, new BeanPropertyRowMapper<>(Balance.class));
    }


    private int saveWithdrawReview1Refuse(Withdraw withdraw) {
        String sql = "UPDATE accounting_withdraw SET wallet_tx_id =  ?, state = ? WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getWallet_tx_id(), withdraw.getState(), withdraw.getUser_id(), withdraw.getId());
    }

    private int saveWithdrawState(Withdraw withdraw) {
        String sql = "UPDATE accounting_withdraw SET state = ? WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getState(), withdraw.getUser_id(), withdraw.getId());
    }

    private int saveWithdrawReview2Refuse(Withdraw withdraw) {
//        withdraw.setRaw_tx(null);
//        withdraw.setState(State.WITHDRAW_REFUSE2);
        String sql = "UPDATE accounting_withdraw SET raw_tx =  ?, state = ? WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getRaw_tx(), withdraw.getState(), withdraw.getUser_id(), withdraw.getId());
    }


    private int saveWithdrawReview1(Withdraw withdraw) {
//        withdraw.setRaw_tx(rawTransaction);
//        withdraw.setState(state);
//        withdraw.setReviewer1_nonce(reviewerNonce);
//        withdraw.setReviewer1_id(reviewerId);
//        withdraw.setReviewer1_signature(reviewerSignature);
//        withdraw.setChange_address(changeAddress == null ? fromAddress : changeAddress);
//        withdraw.setFrom_address(fromAddress);
//        withdraw.setReview1_time(new Timestamp(System.currentTimeMillis()));

        String sql = "UPDATE accounting_withdraw SET raw_tx =  ?, state = ?,reviewer1_nonce = ?, reviewer1_id=?, " +
                "reviewer1_signature=?, change_address=?, from_address=?, review1_time=? , wallet_tx_id=?" +
                "  WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getRaw_tx(), withdraw.getState(), withdraw.getReviewer1_nonce(), withdraw.getReviewer1_id()
                , withdraw.getReviewer1_signature(), withdraw.getChange_address(), withdraw.getFrom_address(), withdraw.getReview1_time(), withdraw.getWallet_tx_id()
                , withdraw.getUser_id(), withdraw.getId());
    }


    private int saveNewWithdraw(Withdraw withdraw) {
//                .user_id(userId)
//                .chain_name(chainName)
//                .currency_name(currencyName)
//                .amount(withdrawAmount)
//                .fee(fee)
//                .to_address(toAddress)
//                .state(State.WITHDRAW_CREATED)
//                .confirm(0)
//                .unread(false)

        String sql = "insert into accounting_withdraw (user_id, chain_name, " +
                "currency_name,amount,fee, to_address, state, memo, confirm ) VALUES(?, ?, ?, ?, ?, ?, ?,?, 0) ";

        return jdbcTemplate.update(sql, withdraw.getUser_id(), withdraw.getChain_name(),
                withdraw.getCurrency_name(), withdraw.getAmount(), withdraw.getFee(), withdraw.getTo_address(), withdraw.getState(), withdraw.getMemo());
    }

    private int saveWithdrawConfirm(Withdraw withdraw) {
//        withdraw.setTxid(chainTxid);
//        withdraw.setConfirm(chainConfirm);
//        withdraw.setState(State.WITHDRAW_BROADCASTED);
        String sql = "UPDATE accounting_withdraw SET  txid =?, confirm=?, state=?, fail_reason=?  WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getTxid(), withdraw.getConfirm(), withdraw.getState(), withdraw.getFail_reason(), withdraw.getUser_id(), withdraw.getId());
    }

    private int saveWithdrawReview2(Withdraw withdraw) {
//        withdraw.setState(state);
//        withdraw.setReviewer2_id(reviewerId);
//        withdraw.setReviewer2_nonce(reviewerNonce);
//        withdraw.setReviewer2_signature(reviewerSignature);
//        withdraw.setReview2_time(new Timestamp(System.currentTimeMillis()));

        String sql = "UPDATE accounting_withdraw SET  state = ?,reviewer2_id = ?, reviewer2_nonce=?, " +
                "reviewer2_signature=?, review2_time=?" +
                "  WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getState(), withdraw.getReviewer2_id(), withdraw.getReviewer2_nonce()
                , withdraw.getReviewer2_signature(), withdraw.getReview2_time()
                , withdraw.getUser_id(), withdraw.getId());
    }

    private int saveWithdrawRawtx(Withdraw withdraw) {
//        withdraw.setRaw_tx(rawTransaction);
        String sql = "UPDATE accounting_withdraw SET  raw_tx = ?, WHERE user_id = ? and id = ? ";
        return jdbcTemplate.update(sql, withdraw.getRaw_tx(), withdraw.getUser_id(), withdraw.getId());
    }

    private int saveBalanceReview1(Balance balance, BigDecimal oldWithdrawing) {
        String sql = "UPDATE accounting_balance SET available =  ?, withdrawing = ? WHERE user_id = ? and withdrawing = ? and currency_name=? ";
        return jdbcTemplate.update(sql, balance.getAvailable(), balance.getWithdrawing(), balance.getUser_id(), oldWithdrawing, balance.getCurrency_name());
    }

    private int saveBalanceWithdrawing(Balance balance, BigDecimal oldWithdrawing) {
        String sql = "UPDATE accounting_balance SET  withdrawing = ? WHERE user_id = ? and withdrawing = ? and currency_name=?";
        return jdbcTemplate.update(sql, balance.getWithdrawing(), balance.getUser_id(), oldWithdrawing, balance.getCurrency_name());
    }


    /**
     * @param states
     * @param page
     * @param size
     * @return
     */
    public List<Withdraw> findAllWithdrawByState(List<Integer> states, Integer page, Integer size) {
        List<Withdraw> all = this.findAllByStateIn(states);
        all.stream()
                .peek(withdraw -> withdraw.setWallet_tx_id(getIdSha256HexString(withdraw)));
        return all;
    }


    public Withdraw findByIdAndUserId(Long id, Long userId) {
        Withdraw withdraw = this.findAllWithdrawByIdAndUserId(id, userId);
        withdraw.setWallet_tx_id(getIdSha256HexString(withdraw));
        return withdraw;
    }

    /**
     * OA一审 --> eden/review1--> wallet/review1 --> OA二审 --> eden/review2 --> wallet/review2
     * |__ 获取: 一审完成 & 无 raw_tx 数据
     * |__ 获取: 二审 完成的
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean review1(Long id,
                           Long userId,
                           Integer state,
                           Long reviewerId,
                           Long reviewerNonce,
                           String reviewerSignature,
                           String fromAddress,
                           String changeAddress) {

        Withdraw withdraw = this.findByIdAndUserId(id, userId);

        if (withdraw == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.DATA_NOT_FOND);
        }

        Integer stateInDb = withdraw.getState();

        if (!Lists.newArrayList(State.WITHDRAW_CREATED, State.WITHDRAW_ERROR_RETRY).contains(stateInDb)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!review1StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        //  "状态不可逆, Irreversible state !");
        String hex = getIdSha256HexString(withdraw);
        withdraw.setWallet_tx_id(hex);

        BigDecimal amount = withdraw.getAmount();
        if (state == State.WITHDRAW_REFUSE1) {
            //一审拒绝
            log.info("review1 一审拒绝中, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());

            Balance balance = this.findBalanceByUserIdAndCurrencyName(userId, withdraw.getCurrency_name());
            BigDecimal oldWithdrawing = balance.getWithdrawing();
            calCancelAmount(withdraw, balance);
            this.saveBalanceReview1(balance, oldWithdrawing);

            withdraw.setState(State.WITHDRAW_REFUSE1);

            this.saveWithdrawReview1Refuse(withdraw);


            log.info("review1 一审拒绝完成, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());

            return true;
        }

        log.info("review1 一审通过中, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());
        //一审 通过
        if (state != State.WITHDRAW_REVIEWED1) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (reviewerNonce == null || reviewerSignature == null)
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        if (fromAddress == null)
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);

        withdraw.setReviewer1_nonce(reviewerNonce);
        withdraw.setReviewer1_signature(reviewerSignature);
        withdraw.setChange_address(changeAddress == null ? fromAddress : changeAddress);

        withdraw.setReviewer1_id(reviewerId);
        withdraw.setFrom_address(fromAddress);
        withdraw.setReview1_time(new Timestamp(System.currentTimeMillis()));


        try {
            JSONObject post = httpService.post(CREATE1URL,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", withdraw.getChain_name(), //chain
                    "currency", withdraw.getCurrency_name(), //currency
                    "txtype", txType.toString(), //txtype
                    "from_address", fromAddress, //from_address
                    "to_address", withdraw.getTo_address(), //to_address
                    "change_address", withdraw.getChange_address(), //change_address
                    "memo", withdraw.getMemo(), //memo
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

            withdraw.setState(state);
            withdraw.setRaw_tx(rawTransaction);

            log.info("sha256Hex={}, saved db ", hex);
            this.saveWithdrawReview1(withdraw);
            log.info("review1 一审通过 完成, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());

            return true;
        } catch (Exception e) {
            log.error(String.format("调用钱包 review1 失败: 256Hex:%s", hex), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_REVIEW1_FAILED, e);

            return false;
        }

    }


    /**
     * OA一审 --> eden/review1--> wallet/review1 --> OA二审 --> eden/review2 --> wallet/review2
     * |__ 异步获取: 一审完成 & 无 raw_tx 数据
     * |__异步获取: 二审 完成的
     *
     * @returreview1n
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean review2(Long id,
                           Long userId,
                           Integer state,
                           Long reviewerId,
                           Long reviewerNonce,
                           String reviewerSignature) {

        Withdraw withdraw = this.findByIdAndUserId(id, userId);
        Integer stateInDb = withdraw.getState();

//        if (WithdrawService.finalStateList.contains(stateInDb)) {
//            //"终极状态不能修改, Final state cannot be modified !");
//            ServiceError.ofAndThrow(ErrorCodeEnum.FINAL_STATE_NOT_MODIFIED);
//        }

        if (stateInDb != State.WITHDRAW_REVIEWED1) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        //先调用一审接口, please call review1 interface !
        if (withdraw.getReviewer1_id() == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PRE_CALL_REVIEW1);
        }


        if (state == State.WITHDRAW_REFUSE2) {
            //调用钱包 walletCancelService 接口
            log.info("review1 2审拒绝中, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());
            if (review2StateList.contains(withdraw.getState()) || finalStateList.contains(withdraw.getState())) {
                ServiceError.ofAndThrow(ErrorCodeEnum.REVIEW2_NOT_CANCEL);
            }
            Balance balance = this.findBalanceByUserIdAndCurrencyName(withdraw.getUser_id(), withdraw.getCurrency_name());
            BigDecimal oldWithdrawing = balance.getWithdrawing();
            walletCancel(withdraw, balance);
            this.saveBalanceReview1(balance, oldWithdrawing);
            this.saveWithdrawReview2Refuse(withdraw);
            log.info("review1 2审拒绝完成, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());
            return true;
        }


        log.info("review1 2审通过中, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());

//        if (state == REVIEW2.getValue()) {
        if (reviewerNonce == null || reviewerSignature == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        }

        withdraw.setReviewer2_nonce(reviewerNonce);
        withdraw.setReviewer2_signature(reviewerSignature);


        //转账2审签名 POST /transaction/review2
        //参数data id(sha256(key+txtype+internaltxid+currency), deduplicate)chain currency reviewer_id2 reviewer_nonce2 reviewer_signature2
        //返回值 id chain currency raw_transaction state review_time

        String hex = getIdSha256HexString(txType, withdraw);
        JSONObject post = httpService.post(CREATE2URL,
                "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                "chain", withdraw.getChain_name(), //chain
                "currency", withdraw.getCurrency_name(), //currency
                "reviewer_id2", reviewerId,//reviewer_id2
                "reviewer_nonce2", reviewerNonce,//reviewer_nonce2
                "reviewer_signature2", reviewerSignature//reviewer_signature2
        );


        withdraw.setState(state);
        withdraw.setReviewer2_id(reviewerId);
        withdraw.setReview2_time(new Timestamp(System.currentTimeMillis()));
        String rawTransaction = post.getString("raw_transaction");

        if (rawTransaction == null) {
            log.error("钱包返回 rawTransaction 异常 rawTransaction is null");
            //钱包返回异常
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        }


        this.saveWithdrawReview2(withdraw);
        log.info("review2 2审通过 完成, hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());
        return true;
    }

    public void walletCancelService(Long id, Long userId) {
        Withdraw withdraw = this.findByIdAndUserId(id, userId);
        if (withdraw.getState() != State.WITHDRAW_CREATED) {
//            throw new RuntimeException("二审无法拒绝");
            ServiceError.ofAndThrow(ErrorCodeEnum.APPLY_REFUSED_FAILED);
        }
        Balance balance = this.findBalanceByUserIdAndCurrencyName(withdraw.getUser_id(), withdraw.getCurrency_name());
        BigDecimal oldWithdrawing = balance.getWithdrawing();
        walletCancel(withdraw, balance);
        this.saveBalanceReview1(balance, oldWithdrawing);
        this.saveWithdrawReview2Refuse(withdraw);
    }

    public void walletCancel(Withdraw withdraw, Balance balance) {
        log.info("walletCancel WalletTxId=hex={}, id={}", withdraw.getWallet_tx_id(), withdraw.getId());

        String hex = getIdSha256HexString(txType, withdraw);
        withdraw.setWallet_tx_id(hex);
        try {
            JSONObject post = httpService.post(CANCELURL,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", withdraw.getChain_name(), //chain
                    "currency", withdraw.getCurrency_name()//currency
            );


            String stateInWallet = post.getString("state");
            if (!"-2".equals(stateInWallet)) {
                //  "钱包取消失败");
                log.error("钱包取消失败, hex:{}", hex);
                ServiceError.ofAndThrow(ErrorCodeEnum.CANCEL_FAILED);
            }
            withdraw.setRaw_tx(null);
            withdraw.setState(State.WITHDRAW_REFUSE2);

            BigDecimal oldWithdrawing = balance.getWithdrawing();
            calCancelAmount(withdraw, balance);
//            this.saveBalanceReview1(balance, oldWithdrawing);
//            this.saveWithdrawReview2Refuse(withdraw);
        } catch (Exception e) {
            log.error(String.format("调用钱包 cancel 失败: 256Hex:%s", hex), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_CANCEL_FAILED, e);
        }


    }

//    /**
//     * 计算拒绝提币资金
//     *
//     * @param withdraw
//     * @param balance
//     */
//    private void calCancelAmount(Withdraw withdraw, Balance balance) {
//        BigDecimal amount = withdraw.getAmount();
//        BigDecimal available = balance.getAvailable();
//        BigDecimal withdrawing = balance.getWithdrawing();
//        BigDecimal fee = withdraw.getFee();
//
//        //set available
//        balance.setAvailable(available.add(amount).add(fee));
//        //set withdraw
//        balance.setWithdrawing(withdrawing.subtract(amount).subtract(fee));
//    }


    /**
     * @param id
     * @return
     */
    public JSONObject getWalletTransaction(Long id) {

        Withdraw withdraw = this.findWithdrawById(id);

        return getWalletTransaction(withdraw);
    }

    private JSONObject getWalletTransaction(Withdraw withdraw) {
        String hex = getIdSha256HexString(txType, withdraw);
        try {
            JSONObject post = httpService.post(TRANSACTION,
                    "id", hex, //id(sha256(key+txtype+internaltxid+currency), deduplicate)
                    "chain", withdraw.getChain_name(), //chain
                    "currency", withdraw.getCurrency_name() //currency
            );
            return post;

        } catch (Exception e) {
            log.error(String.format("getWalletTransaction error: hex=%s, chain=%s, currency=%s", hex, withdraw.getChain_name(), withdraw.getCurrency_name()), e);
            ServiceError.ofAndThrow(ErrorCodeEnum.CALL_TRANSACTION_FAILED, e);
            return null;
        }
    }

    public Withdraw listByIdAndUserId(Long id, Long userId, Integer page, Integer size) {

        if (page > 100 || size > 1000) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR);
        }

        return this.findAllWithdrawByIdAndUserId(id, userId);
    }


    /**
     * 生成摘要, sha256(key + txtype + withdraw_id + currency)
     *
     * @param txType
     * @param withdraw
     * @return
     */
    private String getIdSha256HexString(Integer txType, Withdraw withdraw) {
        return getIdSha256HexString(withdraw);
    }


    /**
     * 校验转账状态
     * 转账成功 job 爬取钱包
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkWithdrawStatus() {

        //需要同步的状态的订单 1,2审
        List<Withdraw> withdrawList = this.findAllByStateIn(jobStateList);

        log.info("transferToSuccess begin , list.size={}", withdrawList.size());
        //处理原则: 能修复即修复, 否则标记为提币失败
        withdrawList.forEach(withdraw -> {
            log.info("正在刷新状态 withdraw in DB  id={}: , hex={}, withdraw.state={} ", withdraw.getId(), withdraw.getWallet_tx_id(), withdraw.getState());

            Integer stateInDb = withdraw.getState();
            Long id = withdraw.getId();
            try {
                JSONObject walletTransaction = getWalletTransaction(withdraw);
                //state /-2已取消 -1未审核 0已审核未签名 1已签名 2广播已发出 3广播失败 4广播完成/
                int stateInWallet = walletTransaction.getIntValue("state");
                if (stateInDb == State.WITHDRAW_REVIEWED1) {

                    String rawTransaction = StringUtils.trimToNull(walletTransaction.getString("rawTransaction"));
                    if (withdraw.getRaw_tx() == null && rawTransaction != null) {
                        withdraw.setRaw_tx(rawTransaction);
                        this.saveWithdrawRawtx(withdraw);
                    }
                } else if (stateInDb == State.WITHDRAW_REVIEWED2) {
                    //state /-2已取消 -1未审核 0已审核未签名 1已签名 2广播已发出 3广播失败 4广播完成/

                    String chainTxid = walletTransaction.getString("chain_txid");
                    int chainConfirm = walletTransaction.getIntValue("chain_confirm");
                    if (stateInWallet == 2) {
                        withdraw.setTxid(chainTxid);
                        withdraw.setConfirm(chainConfirm);
                        withdraw.setState(State.WITHDRAW_REVIEWED2);
                        saveWithdrawConfirm(withdraw);
                    } else if (stateInWallet == 4) {
                        withdraw.setState(State.SUCCESS);
                        withdraw.setTxid(chainTxid);
                        withdraw.setConfirm(chainConfirm);

                        BigDecimal amount = withdraw.getAmount();
                        BigDecimal fee = withdraw.getFee();

                        Balance balance = this.findBalanceByUserIdAndCurrencyName(withdraw.getUser_id(), withdraw.getCurrency_name());
                        saveWithdrawConfirm(withdraw);

                        BigDecimal oldWithdrawing = balance.getWithdrawing();
                        balance.setWithdrawing(oldWithdrawing.subtract(amount).subtract(fee));

                        saveBalanceWithdrawing(balance, oldWithdrawing);
                    } else if (stateInWallet == 3) {//打回重申
                        withdraw.setState(State.WITHDRAW_ERROR_RETRY);
                        withdraw.setTxid(chainTxid);
                        withdraw.setConfirm(chainConfirm);
                        withdraw.setFail_reason(walletTransaction.getString("broadcast_fail_reason"));

                        saveWithdrawConfirm(withdraw);

                    }
                }
            } catch (Exception e) {
                log.error(String.format("钱包处理失败, hex=%s", withdraw.getWallet_tx_id()), e);
            }

        });
    }

    /**
     * 提币列表
     *
     * @param userId
     * @return
     */
    public List<Withdraw> list(Long userId, List<Integer> stateList) {
        return this.findAllWithdrawByUserIdAndStateIn(stateList, userId);
    }

    //    /**
//     * 发起提币请求
//     *
//     * @param userId
//     * @param currencyName
//     * @param amount
//     */
    @Transactional(rollbackFor = Exception.class)
    public void applyWithdraw(Long userId, String currencyName, String chainName, BigDecimal amount, String toAddress, String memo) {

        //String sql = "SELECT scale, min_withdraw_amount FROM tb_currency WHERE name = ?";
        List<DataMap> currencyList = currencyService.query(currencyName);
        DataMap currency = null;////.findAllByCurrencyNameAndChainName(currencyName, chainName);
        for (DataMap dataMap : currencyList) {
            if (chainName.equals(dataMap.getString("chain_name"))) {
                currency = dataMap;
            }
        }

        int scale = currency.getInt("scale");
        BigDecimal min = currency.getBig("min_withdraw_amount");

        if (amount.compareTo(min) < 0 || amount.setScale(scale, BigDecimal.ROUND_DOWN).compareTo(amount) != 0) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR_NOTNULL);
        }

        BigDecimal fee = currency.getBig("withdraw_fee");//"withdraw_fee");

        Balance balance = findBalanceByUserIdAndCurrencyName(userId, currencyName);
        if (balance == null) {
            ServiceError.ofAndThrow(ErrorCodeEnum.DATA_NOT_FOND);
        }
        BigDecimal available = balance.getAvailable();//.getBig("available");

        if (available.compareTo(amount) < 0) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR);
        }

        // 可用额度
        BigDecimal max = withdrawService.queryQuota(userId).getBig("amount");
        if (max.compareTo(amount) < 0) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR);
        }

        // 提币请求
        BigDecimal withdrawAmount = amount.subtract(fee);
        if (withdrawAmount.compareTo(BigDecimal.ZERO) < 0) {
            ServiceError.ofAndThrow(ErrorCodeEnum.PARAMS_ERROR);
        }

        Withdraw withdraw = Withdraw.builder()
                .user_id(userId)
                .chain_name(chainName)
                .currency_name(currencyName)
                .amount(withdrawAmount)
                .fee(fee)
                .to_address(toAddress)
                .state(State.WITHDRAW_CREATED)
                .memo(memo)
                .confirm(0)
                .build();

        saveNewWithdraw(withdraw);
        //version
//        withdraw.setVersion(withdraw.getVersion() + 1);
//        withdraw.setWalletTxId(getIdSha256HexString(txType, Withdraw));


        BigDecimal oldWithdrawing = balance.getWithdrawing();
        balance.setAvailable(available.subtract(amount));
        balance.setWithdrawing(oldWithdrawing.add(amount));

        saveBalanceReview1(balance, oldWithdrawing);
    }

    /**
     * 用户取消提币请求
     *
     * @param userId
     * @param withdrawId
     */
//    @Transactional(rollbackFor = Exception.class)
//    public void cancel(Long userId, Long withdrawId) {
//        Withdraw withdraw = this.findByIdAndUserId(withdrawId, userId);
//        if (withdraw == null) {
//            ServiceError.ofAndThrow(ErrorCodeEnum.DATA_NOT_FOND);
//        }
//
//        if (withdraw.getState() != State.WITHDRAW_CREATED) {
//
//            //终极状态不能修改
//            ServiceError.ofAndThrow(ErrorCodeEnum.APPLY_REFUSED_FAILED);
//        }
//
//        //"UPDATE tb_balance SET available = available + ? WHERE user_id = ? AND currency_name = ?";
//        Balance balance = this.findBalanceByUserIdAndCurrencyName(withdraw.getUser_id(), withdraw.getCurrency_name());
//        BigDecimal oldWithdrawing = balance.getWithdrawing();
//        calCancelAmount(withdraw, balance);
//        this.saveBalanceReview1(balance, oldWithdrawing);
//
////        balance.setAvailable(balance.getAvailable().add(withdraw.getAmount()));
//
//        withdraw.setWallet_tx_id(getIdSha256HexString(txType, withdraw));
//        //sql = "UPDATE tb_transfer SET review_state = 'CANCELED' WHERE id = ? AND type = 2 AND internal = FALSE AND user_id = ? AND review_state = 'CREATED'";
//        withdraw.setState(State.WITHDRAW_CANCELED);
//        this.saveWithdrawState(withdraw);
//
//    }

}
