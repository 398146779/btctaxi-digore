package com.btctaxi.service.tb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import genesis.accounting.config.CommonConfig;
import genesis.accounting.config.RedisKeyConfig;
import genesis.accounting.dao.AccountingBalanceRepository;
import genesis.accounting.dao.AccountingCurrencyRepository;
import genesis.accounting.dao.tb.*;
import genesis.accounting.domain.AccountingBalance;
import genesis.accounting.domain.AccountingCurrency;
import genesis.accounting.domain.tb.*;
import genesis.accounting.domain.vo.SumBalance;
import genesis.common.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static genesis.accounting.config.RedisKeyConfig.CHANGE_RATE_MINUTE;
import static genesis.accounting.config.RedisKeyConfig.CHANGE_TAXI_RATE_MINUTE;

/**
 * User: guangtou
 * Date: 2018/7/6 15:43
 */
@Service
@Slf4j
public class TbCommissionService {

    @Autowired
    private Data data;

    FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd");

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TbUserRepository tbUserRepository;

    @Autowired
    private AccountingBalanceRepository accountingBalanceRepository;

    @Autowired
    private TbRefundDividendDetailRepository tbRefundDividendDetailRepository;

    @Autowired
    private TbRefundCommissionRepository tbRefundCommissionRepository;

    @Autowired
    private TbTransactionRepository tbTransactionRepository;

    @Autowired
    TbOrderRepository tbOrderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TbRefundDividendRepository tbRefundDividendRepository;

    @Autowired
    private TbBalanceSnapshotRepository tbBalanceSnapshotRepository;

    @Autowired
    private AccountingCurrencyRepository accountingCurrencyRepository;

    @PersistenceContext
    @Autowired
    private EntityManager entityManager;


    @Autowired
    private CommonConfig commonConfig;

    /**
     * 生成返佣记录
     * 日期只能到天
     *
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Boolean create(Date endDate) {
        Date startDate = new DateTime(endDate).plusDays(-1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();

        String startDateStr = fastDateFormat.format(startDate);
        String endDateStr = fastDateFormat.format(endDate);
        log.info(" 生成返佣单: startDate={}, endDate={}", startDateStr, endDateStr);
        //获取今天0点到昨天0点的数据,
        //Redis: refund_commission_rate    refund_profit_rate    mine_rate
        ArrayList<String> keys = Lists.newArrayList(
                RedisKeyConfig.CommonConfig.REFUND_COMMISSION_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_COMMISSION_UNIT.name(),
                RedisKeyConfig.CommonConfig.REFUND_LOCKE_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_LOCKE_UNIT.name(),
                RedisKeyConfig.CommonConfig.REFUND_PROFIT_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_PROFIT_UNIT.name(),
                RedisKeyConfig.CommonConfig.DIVIDEND_RATE.name(),
                RedisKeyConfig.CommonConfig.DIVIDEND_UNIT.name(),
                RedisKeyConfig.CommonConfig.MINE_RATE.name(),
                RedisKeyConfig.CommonConfig.MINE_UNIT.name(),
                RedisKeyConfig.CommonConfig.THINK_BIT.name(),
                RedisKeyConfig.CommonConfig.USER_UP_LIMIT.name()
        );
        List<String> config = redisTemplate.<String, String>opsForHash().multiGet(RedisKeyConfig.COMMON_CONFIG, keys);
        config.forEach(s -> {
            log.info("load config by redis , k={}", s);
        });

//        BigDecimal refundCommissionRate = config.get(0) == null ? new BigDecimal("0.015") : new BigDecimal(config.get(0));
//        String refundCommissionUnit = config.get(1) == null ? "IOST" : config.get(1);
//        BigDecimal refundLockeRate = config.get(2) == null ? new BigDecimal("0.25") : new BigDecimal(config.get(2));
//        String refundLockeUnit = config.get(3) == null ? "IOST" : config.get(3);
//        BigDecimal refundProfitRate = config.get(4) == null ? new BigDecimal("0.05") : new BigDecimal(config.get(4));
//        String refundProfitUnit = config.get(5) == null ? "IOST" : config.get(5);
//        BigDecimal dividendRate = config.get(6) == null ? new BigDecimal("0.51") : new BigDecimal(config.get(6));
//        String dividendUnit = config.get(7) == null ? "IOST" : config.get(7);
//        BigDecimal mineRate = config.get(8) == null ? new BigDecimal("1.15") : new BigDecimal(config.get(8));
//        String mineUnit = config.get(9) == null ? "IOST" : config.get(9);
//        String TB = config.get(10) == null ? "IOST" : config.get(10);
//        BigDecimal userUpLimit = config.get(11) == null ? new BigDecimal("50000") : new BigDecimal(config.get(11));

        BigDecimal refundCommissionRate = config.get(0) ==  null ? new BigDecimal("0.50") : new BigDecimal(config.get(0));
        String refundCommissionUnit = config.get(1) == null ? "BTC" : config.get(1);
        BigDecimal refundLockeRate = config.get(2) == null ? new BigDecimal("0") : new BigDecimal(config.get(2));
        String refundLockeUnit = config.get(3) == null ? "BTC" : config.get(3);
        BigDecimal refundProfitRate = config.get(4) == null ? new BigDecimal("0.1") : new BigDecimal(config.get(4));
        String refundProfitUnit = config.get(5) == null ? "BTC" : config.get(5);
        BigDecimal dividendRate = config.get(6) == null ? new BigDecimal("0") : new BigDecimal(config.get(6));
        String dividendUnit = config.get(7) == null ? "BTC" : config.get(7);
        BigDecimal mineRate = config.get(8) == null ? new BigDecimal("0") : new BigDecimal(config.get(8));
        String mineUnit = config.get(9) == null ? "ETH" : config.get(9);
        String TB = config.get(10) == null ? "TB" : config.get(10);
        BigDecimal userUpLimit = config.get(11) == null ? new BigDecimal("50000") : new BigDecimal(config.get(11));


        String sql = "select fee_tb, fee_currency, fee_product, user_id, order_id, create_time, price, amount " +
                " from tb_transaction where create_time >= ? AND create_time < ? and (fee_tb + fee_currency + fee_product) > 0;";
//        sql = "select sum(fee_tb) fee_tb, sum(fee_currency) fee_currency, sum(fee_product) fee_product, sum(amount) amount, user_id, order_id, create_time, price " +
//                "from tb_transaction where create_time >= ? AND create_time < ? and (fee_tb + fee_currency + fee_product) > 0 group by order_id, user_id, create_time, price";
        //group by user_id, order_id, create_time; 获取所有有佣金用户
        List<Map<String, Object>> allTransactionList = data.queryForList(sql, new Object[]{startDateStr, endDateStr});
        //计算汇率: TB_BTC
        // 调用接口 ,  获取汇率, 转换 feeTb feeCurrency feeProduct --> TB / BTC
        //String result = "{\"EOS\": \"0.00576\", \"USDT\": \"0.6657\", \"BTC\": \"0.000006657\" , \"TB\": \"0.000006657\", \"ETH\": \"0.000006657\"}";
        HashMap<String, BigDecimal> changeRate = getAvgChangeRate(startDateStr);//获取 createTime 小时汇率均值
        //EOS_BTC
        log.info(" startDateStr = {} 当前平均汇率={}", startDateStr, changeRate);

        Map<Long, SumBalance> list = allTransactionList.stream()
                .map(o -> {
                    SumBalance sumBalance = new SumBalance();
                    try {
                        BeanUtils.populate(sumBalance, o);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error(String.format("BeanUtils.populate error, o = %s ", o), e);
                    }
                    return sumBalance;
                })
                .peek(sumBalance -> log.debug("sumBalance = {}", sumBalance))
                .collect(Collectors.toMap(SumBalance::getOrder_id, Function.identity(), (o, o2) -> {
//                    if (o == null || o2 == null ) return ;
                            o2.setFee_tb(o.getFee_tb().add(o2.getFee_tb()));
                            o2.setFee_currency(o.getFee_currency().add(o2.getFee_currency()));
                            o2.setFee_product(o.getFee_product().add(o2.getFee_product()));
                            o2.setPrice(o.getPrice().add(o2.getPrice()));
                            o2.setAmount(o.getAmount().add(o2.getAmount()));
                            return o2;
                        })
                );


        TbRefundCommission refundCommission = new TbRefundCommission();

        HashMap<String, AccountingCurrency> currencyMap = Maps.newHashMap();
        List<AccountingCurrency> currencyList = accountingCurrencyRepository.findAll();
        log.info("currencyList.size={}", currencyList.size());
        currencyList.forEach(currency -> currencyMap.put(currency.getCurrencyName(), currency));


        for (Map.Entry<Long, SumBalance> obj : list.entrySet()) {
            Long aLong = obj.getKey();
            SumBalance it = obj.getValue();


            log.debug("group sumBalance k = {}, v = {}", aLong, it);
            BigDecimal feeTb = it.getFee_tb();
            BigDecimal feeCurrency = it.getFee_currency();
            BigDecimal feeProduct = it.getFee_product();
            Long userId = it.getUser_id();
            Long orderId = it.getOrder_id();
            Date createTime = it.getCreate_time();
            BigDecimal price = it.getPrice();
            BigDecimal amount = it.getAmount();
            log.debug("allTransactionList.size={}, orderId = {}, feeTb={}, feeCurrency={}, feeProduct={}, userId={}, orderId={}, createTime={}, amount={}",
                    allTransactionList.size(), orderId, feeTb, feeCurrency, feeProduct, userId, orderId, createTime, amount);
            try {


                //生成挖矿记录
                //sub_user_id   sub_email   invitor_id  invitor_email
                TbUser user = tbUserRepository.getOne(userId);
                Long invitorId = user.getInvitorId();

                TbUser invitorUser = null;
                if (invitorId != null) {
                    invitorUser = tbUserRepository.getOne(invitorId);
                }

                //pair_name
                TbOrder order = tbOrderRepository.findByUserIdAndIdAndCreateTimeLessThanEqual(userId, orderId, createTime);
                String pairName = order.getPairName();

                String[] pairArray = pairName.split("_");


                BigDecimal pairCurrencyChangeRate = changeRate.get(pairArray[1]) == null ? BigDecimal.ZERO : changeRate.get(pairArray[1]);
                BigDecimal pairProductChangeRate = changeRate.get(pairArray[0]) == null ? BigDecimal.ZERO : changeRate.get(pairArray[0]);
                BigDecimal tbPairChangeRate = changeRate.get(TB) == null ? BigDecimal.ZERO : changeRate.get(TB);

                BigDecimal commissionBTCAmount = feeTb.multiply(tbPairChangeRate)
                        .add(feeCurrency.multiply(pairCurrencyChangeRate))
                        .add(feeProduct.multiply(pairProductChangeRate));

                //除以目标比汇率 refundCommissionUnit ????????????????????
                BigDecimal refundCommissionAmount = BigDecimal.ZERO;
                BigDecimal refundProfitAmount = BigDecimal.ZERO;
                BigDecimal refundLockAmount = BigDecimal.ZERO;


                if (invitorId != null) {
                    refundProfitAmount = commissionBTCAmount.multiply(refundProfitRate).divide(changeRate.get(refundProfitUnit), currencyMap.get(refundProfitUnit).getScale(), BigDecimal.ROUND_DOWN);
                }
                refundCommissionAmount = commissionBTCAmount.multiply(refundCommissionRate).divide(changeRate.get(refundCommissionUnit), currencyMap.get(refundCommissionUnit).getScale(), BigDecimal.ROUND_DOWN);
                refundLockAmount = commissionBTCAmount.multiply(refundLockeRate).divide(changeRate.get(refundLockeUnit), currencyMap.get(refundLockeUnit).getScale(), BigDecimal.ROUND_DOWN);
                BigDecimal mineAmount = commissionBTCAmount.multiply(mineRate).divide(changeRate.get(mineUnit), currencyMap.get(mineUnit).getScale(), BigDecimal.ROUND_DOWN);

                refundCommission = TbRefundCommission.builder()
                        .subUserId(userId).subEmail(user.getEmail())
                        .invitorId(invitorId).invitorEmail(invitorUser == null ? null : invitorUser.getEmail())
                        .feeTb(feeTb).feeCurrency(feeCurrency).feeProduct(feeProduct).direction(order.getDirection())
                        .pairName(pairName)
                        .refundCommissionAmount(refundCommissionAmount).refundCommissionUnit(refundCommissionUnit)
                        .refundLockAmount(refundLockAmount).refundLockUnit(refundLockeUnit)
                        .refundProfitAmount(refundProfitAmount).refundProfitUnit(refundProfitUnit)
                        //  .dividendAmount(BigDecimal.ZERO)
                        //  .dividendUnit(dividendUnit)
                        .mineAmount(mineAmount.min(userUpLimit)).mineUnit(mineUnit)
                        .amount(amount).price(price)
                        .createTime(createTime).hour(createTime.getHours())
                        .orderId(orderId)
                        .commissionBtcAmount(commissionBTCAmount)
                        .build();

                tbRefundCommissionRepository.save(refundCommission);


            } catch (Exception e) {
                log.error(String.format("create error : userId = %s, refundCommission=%s", userId, refundCommission.toString()), e);
                throw e;
            }

        }

        return true;

    }


    /**
     * Taxi 交易所
     * 分红: 持有平台币的用户进行分红, 分交易币的51%
     * 单位最小为1天
     */
//    @Transactional
//    public Boolean createDividend(Date startDate, Date endDate) {
////        /usr/bin/curl "http://localhost:2000/deposit/check"
//
//        AtomicBoolean isSuccess = new AtomicBoolean(true);
//        List<String> config = redisTemplate.<String, String>opsForHash().multiGet(RedisKeyConfig.COMMON_CONFIG,
//                Lists.newArrayList(
//                        RedisKeyConfig.CommonConfig.DIVIDEND_RATE.name(),
//                        RedisKeyConfig.CommonConfig.THINK_BIT.name(),
//                        RedisKeyConfig.CommonConfig.DIVIDEND_UNIT.name()
//                )
//        );
//
//        HashMap<String, AccountingCurrency> currencyMap = Maps.newHashMap();
//        accountingCurrencyRepository.findAll().forEach(currency -> currencyMap.put(currency.getCurrencyName(), currency));
//
//
//        BigDecimal dividendRate = config.get(0) == null ? new BigDecimal(0.51) : new BigDecimal(config.get(0));
//        String THINK_BIT = config.get(1) == null ? "IOST" : config.get(1);
//        String DIVIDEND_UNIT = config.get(2) == null ? "BTC" : config.get(2);
//
//        //2. 计算每个币种 用户最小持仓量
//        String balanceRateSql = " select MIN(account_balance) account_balance , currency, uid  from tb_balance_snapshot " +
//                " where create_time >= ? and create_time < ? and currency = ? group by uid, currency";
//
//        List<Map<String, Object>> userBalance = data.queryForList(balanceRateSql, new Object[]{startDate, endDate, THINK_BIT});
//
//        //String result = "{\"EOS\": \"0.00576\", \"USDT\": \"0.6657\", \"BTC\": \"0.000006657\" , \"TB\": \"0.000006657\", \"ETH\": \"0.000006657\"}";
////        JSONObject changeRate = restTemplate.getForObject(changeRateUrl, JSONObject.class);
//        HashMap<String, BigDecimal> changeRate = getAvgChangeRate(fastDateFormat.format(endDate));//获取 createTime 小时汇率均值
//        log.info(" 当前汇率={}", changeRate);
//
//
//        //计算每个币种  最小 持仓量 总额
//        HashMap<String, BigDecimal> sumCurrencyBalanceMap = Maps.newHashMap();
//        userBalance.forEach(map -> {
//            String currency = (String) map.get("currency");
//            BigDecimal v = sumCurrencyBalanceMap.get(currency);
//            if (v == null) v = BigDecimal.ZERO;
//            v = v.add(((BigDecimal) map.get("account_balance")));
//            sumCurrencyBalanceMap.put(currency, v);
//        });
//
//        String commissionBtcSql = "select sum(commission_btc_amount) commission_btc_amount from tb_refund_commission a where a.create_time >= ? and a.create_time < ? ";
//        DataMap commissionBtcAmount = data.queryOne(commissionBtcSql, startDate, endDate);
//        BigDecimal sumCommissionAmount = commissionBtcAmount.getBig("commission_btc_amount");
//
//        Date now = new Date();
//        // 用户最小持仓量 / 最小持仓量总额
//        for (Map<String, Object> o : userBalance) {
//            BigDecimal minAccountBalance = (BigDecimal) o.get("account_balance");
////            String currency = (String) o.get("currency");
//            Long userId = (Long) o.get("uid");
//            TbRefundDividend refundDividend = tbRefundDividendRepository.findByUidAndDataTimeGreaterThanEqualAndDataTimeLessThan(userId, startDate, endDate);
//            try {
//
//                //公式 最小持仓 / 所有人持仓总量 * 分成比例 * 当前币所有佣金 / 当前汇率
//                BigDecimal currencyChangeRate = changeRate.get(DIVIDEND_UNIT);
//                BigDecimal dividend = minAccountBalance.divide(sumCurrencyBalanceMap.get(THINK_BIT), BigDecimal.ROUND_DOWN) // 当前币种 最小持仓/持仓总量
//                        .multiply(dividendRate) //
//                        .multiply(sumCommissionAmount)
//                        .divide(currencyChangeRate, currencyMap.get(DIVIDEND_UNIT).getScale(), BigDecimal.ROUND_DOWN);
//
//
//                //计算 //查询用户 , crate, hore, 所有订单
//                if (dividend.compareTo(BigDecimal.ZERO) <= 0) {
//                    continue;
//                }
//
//                if (refundDividend == null) {
//                    refundDividend = TbRefundDividend.builder()
//                            .uid(userId)
//                            .dividendAmount(BigDecimal.ZERO)
//                            .dividendUnit(DIVIDEND_UNIT)
//                            .dataTime(startDate)
//                            .createTime(now)
//                            .state(0)
//                            .build();
//                }
//
//                refundDividend.setDividendAmount(refundDividend.getDividendAmount().add(dividend));
//
//
//                tbRefundDividendRepository.save(refundDividend);
//
//            } catch (Exception e) {
//                isSuccess.set(false);
//                log.error("createDividend error :  " + refundDividend.toString(), e);
//            }
//        }
//        return isSuccess.get();
//    }
    @Transactional(rollbackFor = Throwable.class)
    public Boolean createDividend2(Date startDate, Date endDate) {

        //矿池所有人
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        List<String> config = redisTemplate.<String, String>opsForHash().multiGet(RedisKeyConfig.COMMON_CONFIG,
                Lists.newArrayList(
                        RedisKeyConfig.CommonConfig.DIVIDEND_RATE.name(),
                        RedisKeyConfig.CommonConfig.THINK_BIT.name(),
                        RedisKeyConfig.CommonConfig.MINER_OWNER_ID_LIST.name(),
                        RedisKeyConfig.CommonConfig.MINER_OWNER_RATE_RATE.name()
                )
        );

        BigDecimal dividendRate = config.get(0) == null ? new BigDecimal(0.51) : new BigDecimal(config.get(0));
        String THINK_BIT = config.get(1) == null ? "TAXI" : config.get(1);
        List<String> USER_EMAIL_LIST = config.get(2) == null ? Lists.newArrayList() : Lists.newArrayList(config.get(2).split(","));
        BigDecimal USER_RATE_RATE = config.get(3) == null ? new BigDecimal(0.1) : new BigDecimal(config.get(3));

        //2. 计算每个币种 用户最小持仓量
        String balanceRateSql = " select MIN(account_balance) account_balance , currency, uid  from tb_balance_snapshot " +
                " where create_time >= ? and create_time < ? and currency = ? group by uid, currency";

        List<Map<String, Object>> userBalanceList = data.queryForList(balanceRateSql, new Object[]{startDate, endDate, THINK_BIT});

//        HashMap<String, BigDecimal> changeRate = getAvgChangeRate(fastDateFormat.format(endDate));//获取 createTime 小时汇率均值
//        log.info(" 当前汇率={}", changeRate);


        //计算每个币种  最小 持仓量 总额
        HashMap<String, BigDecimal> sumCurrencyBalanceMap = Maps.newHashMap();
        userBalanceList.forEach(map -> {
            String currency = (String) map.get("currency");
            BigDecimal v = sumCurrencyBalanceMap.get(currency);
            if (v == null) v = BigDecimal.ZERO;
            v = v.add(((BigDecimal) map.get("account_balance")));
            sumCurrencyBalanceMap.put(currency, v);
        });

        //TODO 可能存在问题 能查出 几千万佣金
        String commissionBtcSql = "select currency_name, sum(fee_currency) fee_currency from tb_transaction where create_time>=? and create_time < ? group by currency_name ";
        String commissionBtcSql2 = "select product_name currency_name , sum(fee_product) fee_currency from tb_transaction where create_time>=? and create_time < ? group by product_name; ";
        List<Map<String, Object>> commissionBtcAmount = data.queryForList(commissionBtcSql, startDate, endDate);
        List<Map<String, Object>> commissionBtcAmount2 = data.queryForList(commissionBtcSql2, startDate, endDate);

        commissionBtcAmount.addAll(commissionBtcAmount2);

        Date now = new Date();
        // 用户最小持仓量 / 最小持仓量总额
        for (Map<String, Object> o : userBalanceList) {
            BigDecimal minAccountBalance = (BigDecimal) o.get("account_balance");
            Long userId = (Long) o.get("uid");

            for (Map<String, Object> map : commissionBtcAmount) {
                String currencyName = (String) map.get("currency_name");
                BigDecimal sumFeeCurrency = (BigDecimal) map.get("fee_currency");


                BigDecimal userDividendRate = dividendRate;
                if (USER_EMAIL_LIST.contains(userId + "")) {
                    userDividendRate = dividendRate.add(USER_RATE_RATE);
                }
                try {
                    //公式 最小持仓 / 所有人持仓总量 * 分成比例
                    // 当前币种 最小持仓/持仓总量
                    BigDecimal rate = minAccountBalance.divide(sumCurrencyBalanceMap.get(THINK_BIT), BigDecimal.ROUND_DOWN);
                    BigDecimal dividendAmount = rate
                            .multiply(userDividendRate)
                            .multiply(sumFeeCurrency);


                    //计算 //查询用户 , crate, hore, 所有订单
                    if (dividendAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    log.info(" 分红:  userId={}, currencyName={}, dividendAmount={}, sumFeeCurrency={}, startDate={}, rate={} ", userId, currencyName, dividendAmount.toPlainString(),sumFeeCurrency, startDate, rate.toPlainString());

//                    AccountingBalance mineBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, currencyName);
//                    AccountingBalance entity = genBalance(userId, currencyName, dividendAmount, mineBalance);
//                    accountingBalanceRepository.save(entity);

                    TbRefundDividendDetail dividendDetail = TbRefundDividendDetail.builder()
                            .uid(userId)
                            .dividendAmount(dividendAmount)
                            .dividendUnit(currencyName)
                            .dataTime(startDate)
                            .state(0)
                            .createTime(new Timestamp(System.currentTimeMillis()))
                            .build();
                    tbRefundDividendDetailRepository.save(dividendDetail);

                } catch (Exception e) {
                    isSuccess.set(false);
                    log.error("createDividend error :  ", e);
                    throw e;
                }


            }

        }


        return isSuccess.get();
    }


    /**
     * 分红 结算
     * @param startDate
     * @param endDate
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Boolean refundDividend(Date startDate, Date endDate) {
        List<TbRefundDividendDetail> list = tbRefundDividendDetailRepository.findAllByDataTimeGreaterThanEqualAndDataTimeLessThanAndState(startDate, endDate, 0);
        list.forEach(it->{
            long userId = it.getUid();
            String currencyName = it.getDividendUnit();
            BigDecimal dividendAmount = it.getDividendAmount();

            AccountingBalance mineBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, currencyName);
            AccountingBalance entity = genBalance(userId, currencyName, dividendAmount, mineBalance);
            accountingBalanceRepository.save(entity);
            it.setState(1);
            tbRefundDividendDetailRepository.save(it);
        });

        return true;
    }



    @Transactional(rollbackFor = Throwable.class)
    public Boolean createDividend3(Date startDate, Date endDate) {

        //矿池所有人
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        List<String> config = redisTemplate.<String, String>opsForHash().multiGet(RedisKeyConfig.COMMON_CONFIG,
                Lists.newArrayList(
                        RedisKeyConfig.CommonConfig.DIVIDEND_RATE.name(),
                        RedisKeyConfig.CommonConfig.THINK_BIT.name(),
                        RedisKeyConfig.CommonConfig.MINER_OWNER_ID_LIST.name(),
                        RedisKeyConfig.CommonConfig.MINER_OWNER_RATE_RATE.name()
                )
        );

        BigDecimal dividendRate = config.get(0) == null ? new BigDecimal(0.51) : new BigDecimal(config.get(0));
        String THINK_BIT = config.get(1) == null ? "TAXI" : config.get(1);
        List<String> USER_EMAIL_LIST = config.get(2) == null ? Lists.newArrayList() : Lists.newArrayList(config.get(2).split(","));
        BigDecimal USER_RATE_RATE = config.get(3) == null ? new BigDecimal(0.1) : new BigDecimal(config.get(3));

        //2. 计算每个币种 用户最小持仓量
        String balanceRateSql = " select MIN(account_balance) account_balance , currency, uid  from tb_balance_snapshot " +
                " where create_time >= ? and create_time < ? and currency = ? group by uid, currency";

        List<Map<String, Object>> userBalanceList = data.queryForList(balanceRateSql, new Object[]{startDate, endDate, THINK_BIT});

        //计算每个币种  最小 持仓量 总额
        HashMap<String, BigDecimal> sumCurrencyBalanceMap = Maps.newHashMap();
        userBalanceList.forEach(map -> {
            String currency = (String) map.get("currency");
            BigDecimal v = sumCurrencyBalanceMap.get(currency);
            if (v == null) v = BigDecimal.ZERO;
            v = v.add(((BigDecimal) map.get("account_balance")));
            sumCurrencyBalanceMap.put(currency, v);
        });

        String commissionBtcSql = "select currency_name, sum(fee_currency) fee_currency from tb_transaction where create_time>=? and create_time < ? group by currency_name ";
        String commissionBtcSql2 = "select product_name currency_name , sum(fee_product) fee_currency from tb_transaction where create_time>=? and create_time < ? group by product_name; ";
        List<Map<String, Object>> commissionBtcAmount = data.queryForList(commissionBtcSql, startDate, endDate);
        List<Map<String, Object>> commissionBtcAmount2 = data.queryForList(commissionBtcSql2, startDate, endDate);

        commissionBtcAmount.addAll(commissionBtcAmount2);

        Date now = new Date();
        // 用户最小持仓量 / 最小持仓量总额
        for (Map<String, Object> o : userBalanceList) {
            BigDecimal minAccountBalance = (BigDecimal) o.get("account_balance");
            Long userId = (Long) o.get("uid");

            for (Map<String, Object> map : commissionBtcAmount) {
                String currencyName = (String) map.get("currency_name");
                BigDecimal sumFeeCurrency = (BigDecimal) map.get("fee_currency");


                BigDecimal userDividendRate = dividendRate;
                if (USER_EMAIL_LIST.contains(userId + "")) {
                    userDividendRate = dividendRate.add(USER_RATE_RATE);
                }
                try {
                    //公式 最小持仓 / 所有人持仓总量 * 分成比例
                    // 当前币种 最小持仓/持仓总量
                    BigDecimal rate = minAccountBalance.divide(sumCurrencyBalanceMap.get(THINK_BIT), BigDecimal.ROUND_DOWN);
                    BigDecimal dividendAmount = rate
                            .multiply(userDividendRate)
                            .multiply(sumFeeCurrency);


                    //计算 //查询用户 , crate, hore, 所有订单
                    if (dividendAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    log.info(" 3分红:  userId={}, currencyName={}, dividendAmount={}, sumFeeCurrency={}, startDate={}, rate={} ", userId, currencyName, dividendAmount.toPlainString(),sumFeeCurrency, startDate, rate.toPlainString());

                } catch (Exception e) {
                    isSuccess.set(false);
                    log.error(" 3createDividend error :  ", e);
                    throw e;
                }


            }

        }


        return isSuccess.get();
    }



    /**
     * 资产快照 10分钟执行一次
     */
    @Transactional(rollbackFor = Throwable.class)
    public Boolean balanceSnapshot() {
        String currency = redisTemplate.<String, String>opsForHash().get(RedisKeyConfig.COMMON_CONFIG, RedisKeyConfig.CommonConfig.THINK_BIT.name());

        if (StringUtils.isEmpty(currency)) {
            log.error(" 平台币为设置: ");
            currency = "TB";
        }
        List<AccountingBalance> list = accountingBalanceRepository.findAllByAvailableGreaterThanAndCurrencyName(BigDecimal.ZERO, currency);
        List<Long> userList = list.stream().map(AccountingBalance::getUserId).collect(Collectors.toList());


        List<AccountingBalance> balanceSnapshot = accountingBalanceRepository.findAllByUserIdInAndAvailableGreaterThan(userList, BigDecimal.ZERO);

//        entityManager
        balanceSnapshot.stream().map(balance -> {
            return TbBalanceSnapshot.builder()
                    .uid(balance.getUserId())
                    .currency(balance.getCurrencyName())
                    .accountBalance(balance.getAvailable().add(balance.getLocking()))
                    .createTime(new Date())
                    .build();
        }).forEachOrdered(tbBalanceSnapshot -> {
            tbBalanceSnapshotRepository.save(tbBalanceSnapshot);
        });

        return true;
    }


    public HashMap<String, BigDecimal> getAvgChangeRate(String day) {
        String k = CHANGE_RATE_MINUTE + day;

        List<String> changeRateMinute = redisTemplate.opsForList().range(k, 0, -1);

        ArrayListMultimap<String, BigDecimal> multimap = ArrayListMultimap.create();

        changeRateMinute.stream().forEach(s -> {
            JSONObject jsonObject = JSON.parseObject(s);
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                multimap.put(m.getKey(), new BigDecimal((String) m.getValue()));
            }
        });

        HashMap<String, BigDecimal> avgChangeRate = Maps.newHashMap();
        multimap.keySet().forEach(s -> {
            Double average = multimap.get(s).stream()
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .getAsDouble();
            avgChangeRate.put(s, new BigDecimal(average).setScale(12, BigDecimal.ROUND_DOWN));
        });

        if (CollectionUtils.isEmpty(avgChangeRate)) {
            restTemplate.getForObject(commonConfig.getChangeRateUrl(), JSONObject.class)
                    .forEach((key, value) -> avgChangeRate.put(key, new BigDecimal(value.toString())));
            log.info("avgChangeRate is empty, use current ChangeRate , avgChangeRate = {}", avgChangeRate);
        }

        String taxiStr = redisTemplate.opsForValue().get(CHANGE_TAXI_RATE_MINUTE);
        if (StringUtils.isNotEmpty(taxiStr)) {
            avgChangeRate.put("TAXI", new BigDecimal(taxiStr));
            log.info(" TAXI 指定结算汇率 = {}", taxiStr);
        }

        return avgChangeRate;
    }


    /**
     * 发奖励
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Boolean refund(Date startDate, Date endDate) {

//        INSERT INTO gate.tb_refund_dividend (id, uid, email, inviter_id, inviter_email, refund_commission_amount, refund_commission_unit, refund_lock_amount, refund_lock_unit, refund_profit_amount, refund_profit_unit, dividend_amount, dividend_unit, mine_amount, mine_unit, create_time, data_time) VALUES (3, 12, null, null, null, 0.000000000000, null, 0.000000000000, null, 0.000000000000, null, 0.000020733427, 'BTC', null, null, '2018-07-19 07:53:45', '2018-07-17');
        List<TbRefundDividend> list = tbRefundDividendRepository.findAllByDataTimeGreaterThanEqualAndDataTimeLessThanAndState(startDate, endDate, 0);


        for (TbRefundDividend o : list) {

            //1. 发奖励 分红 2. 发奖励 返佣, 返利, 挖矿
            //分红 + 旷工 + 返佣 + 返利
            //3. 发奖励 锁量
            if (o.getState() == 1) {
                continue;
            }

            o.setState(1);

            //查询 balance
            Long userId = o.getUid();
            BigDecimal dividendAmount = o.getDividendAmount();
            if (dividendAmount != null && dividendAmount.compareTo(BigDecimal.ZERO) > 0) {
                String dividendUnit = o.getDividendUnit();
                AccountingBalance balance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, dividendUnit);
                AccountingBalance entity = genBalance(userId, dividendUnit, dividendAmount, balance);
                accountingBalanceRepository.save(entity);
                log.info("refund 分红 发奖 , entity={}", entity);
            }


            BigDecimal mineAmount = o.getMineAmount();
            if (mineAmount != null && mineAmount.compareTo(BigDecimal.ZERO) > 0) {
                String mineUnit = o.getMineUnit();
                AccountingBalance mineBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, mineUnit);
                AccountingBalance entity = genBalance(userId, mineUnit, mineAmount, mineBalance);
                accountingBalanceRepository.save(entity);
                log.info("refund 旷工 发奖 , entity={}", entity);

            }

            BigDecimal refundCommissionAmount = o.getRefundCommissionAmount();
            if (refundCommissionAmount != null && refundCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
                String refundCommissionUnit = o.getRefundCommissionUnit();
                AccountingBalance refundCommissionBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, refundCommissionUnit);
                AccountingBalance entity = genBalance(userId, refundCommissionUnit, refundCommissionAmount, refundCommissionBalance);
                accountingBalanceRepository.save(entity);
                log.info("refund 返佣 发奖 , entity={}", entity);

            }

            BigDecimal refundProfitAmount = o.getRefundProfitAmount();
            if (refundProfitAmount != null && refundProfitAmount.compareTo(BigDecimal.ZERO) > 0) {
                String refundProfitUnit = o.getRefundProfitUnit();
                AccountingBalance refundProfitBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, refundProfitUnit);
                AccountingBalance entity = genBalance(userId, refundProfitUnit, refundProfitAmount, refundProfitBalance);
                accountingBalanceRepository.save(entity);
                log.info("refund 返利 发奖 , entity={}", entity);

            }


            BigDecimal lockAmount = o.getRefundLockAmount();
            if (lockAmount != null && lockAmount.compareTo(BigDecimal.ZERO) > 0) {

                String lockUnit = o.getRefundLockUnit();
                AccountingBalance lockBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(userId, lockUnit);
                if (lockBalance == null) {
                    lockBalance = AccountingBalance.builder().userId(userId).currencyName(lockUnit)
                            .withdrawing(BigDecimal.ZERO).locking(BigDecimal.ZERO).available(BigDecimal.ZERO).ordering(BigDecimal.ZERO)
                            .build();
                }
                lockBalance.setLocking(lockBalance.getLocking().add(lockAmount));
                accountingBalanceRepository.save(lockBalance);
                log.info("refund 锁量 发奖 , entity={}", lockBalance);

            }


            tbRefundDividendRepository.save(o);
        }

        return true;
    }

    private AccountingBalance genBalance(Long userId, String dividendUnit, BigDecimal amount, AccountingBalance dividendBalance) {
        if (dividendBalance == null) {
            dividendBalance = AccountingBalance.builder()
                    .userId(userId)
                    .withdrawing(BigDecimal.ZERO)
                    .locking(BigDecimal.ZERO)
                    .available(BigDecimal.ZERO)
                    .ordering(BigDecimal.ZERO)
                    .currencyName(dividendUnit)
                    .build();
        }
        dividendBalance.setAvailable(dividendBalance.getAvailable().add(amount));

        return dividendBalance;
    }

}
