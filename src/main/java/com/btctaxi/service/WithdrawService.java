package com.btctaxi.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genesis.accounting.config.CommonConfig;
import genesis.accounting.config.State;
import genesis.accounting.config.WalletConfig;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class WithdrawService {
    private Data data;
    private WalletConfig walletConfig;
    private CommonConfig commonConfig;
    private HttpService http;
    private RestTemplate restTemplate;

    public WithdrawService(Data data, WalletConfig walletConfig, HttpService http, CommonConfig commonConfig, RestTemplate restTemplate) {
        this.data = data;
        this.walletConfig = walletConfig;
        this.http = http;
        this.commonConfig = commonConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * 查询用户可用额度
     */
    @Transactional(readOnly = true)
    public DataMap queryQuota(long userId) {
        String sql = "SELECT amount FROM accounting_withdraw_quota WHERE user_id = ?";
        DataMap userQuota = data.queryOne(sql, userId);
        if (userQuota == null) {
            userQuota = new DataMap();
            userQuota.put("amount", commonConfig.getWithdrawQuota());
        }

        BigDecimal quota = userQuota.getBig("amount");
        BigDecimal recent24BTC = recent24BTC(userId);
        BigDecimal quotaBTC = quota.subtract(recent24BTC).max(BigDecimal.ZERO);
        userQuota.put("amount", quotaBTC);

        return userQuota;
    }

    /**
     * 更新用户可用额度
     */
    @Transactional(rollbackFor = Throwable.class)
    public void updateQuota(long userId, BigDecimal amount) {
        String sql = "INSERT INTO accounting_withdraw_quota(user_id, amount, update_time, manual) VALUES(?, ?, NOW(), TRUE) ON DUPLICATE KEY UPDATE amount = ?, update_time = NOW(), manual = TRUE";
        data.update(sql, userId, amount, amount);
    }

    private BigDecimal recent24BTC(long userId) {
        Timestamp dayAgo = new Timestamp(System.currentTimeMillis() - 86400000L);
        String sql = "SELECT currency_name, SUM(amount + fee) AS amount FROM accounting_withdraw WHERE user_id = ? AND create_time > ? AND state > 0 GROUP BY currency_name";
        List<DataMap> withdraws = data.query(sql, userId, dayAgo);
        BigDecimal sum = equalityBTC(withdraws);
        return sum;
    }

    //等值BTC
    private BigDecimal equalityBTC(List<DataMap> withdraws) {
        ResponseEntity<String> res = restTemplate.getForEntity(commonConfig.getChangeRateUrl(), String.class);
        JSONObject rates = JSON.parseObject(res.getBody());
        rates.put("BTC", BigDecimal.ONE); //防止BTC不存在

        BigDecimal sum = BigDecimal.ZERO;
        for (DataMap withdraw : withdraws) {
            String currencyName = withdraw.getString("currency_name");
            BigDecimal amount = withdraw.getBig("amount");
            BigDecimal rate = rates.getBigDecimal(currencyName);
            rate = rate == null ? BigDecimal.ZERO : rate;
            sum = sum.add(amount.multiply(rate));
        }
        return sum;
    }

    /**
     * 创建提币申请
     */
    @Transactional(rollbackFor = Throwable.class)
    public void create(long userId, String chainName, String currencyName, String address, String memo, BigDecimal amount) {
        String sql = "SELECT scale, min_withdraw_amount, withdraw_fee, withdrawable FROM accounting_currency WHERE chain_name = ? AND currency_name = ?";
        DataMap currency = data.queryOne(sql, chainName, currencyName);
        int scale = currency.getInt("scale");
        BigDecimal minWithdrawAmount = currency.getBig("min_withdraw_amount");
        BigDecimal withdrawFee = currency.getBig("withdraw_fee");
        boolean withdrawable = currency.getBoolean("withdrawable");
        if (!withdrawable)
            throw new RuntimeException("withdraw is disabled");

        amount = amount.setScale(scale, BigDecimal.ROUND_DOWN);
        if (amount.compareTo(minWithdrawAmount) < 0)
            throw new RuntimeException("invalid amount");

        sql = "SELECT available FROM accounting_balance WHERE user_id = ? AND currency_name = ?";
        DataMap balance = data.queryOne(sql, userId, currencyName);
        BigDecimal available = balance.getBig("available");

        if (available.compareTo(amount) < 0)
            throw new RuntimeException("invalid amount");

        DataMap userQuota = queryQuota(userId);
        BigDecimal quotaAmount = userQuota.getBig("amount");

        ResponseEntity<String> res = restTemplate.getForEntity(commonConfig.getChangeRateUrl(), String.class);
        JSONObject rates = JSON.parseObject(res.getBody());
        rates.put("BTC", BigDecimal.ONE); //防止BTC不存在
        BigDecimal rate = rates.getBigDecimal(currencyName);
        BigDecimal quota = quotaAmount.divide(rate, scale, BigDecimal.ROUND_DOWN);

        if (amount.compareTo(quota) > 0)
            throw new RuntimeException("invalid quota " + userId + " " + amount.toPlainString() + " " + quota);

        BigDecimal realAmount = amount.subtract(withdrawFee);

        sql = "INSERT INTO accounting_withdraw(user_id, chain_name, currency_name, to_address, memo, amount, fee, state) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        long id = data.insert(sql, userId, chainName, currencyName, address, memo, realAmount, withdrawFee, State.WITHDRAW_CREATED);

        sql = "UPDATE accounting_balance SET available = available - ?, withdrawing = withdrawing + ? WHERE user_id = ? AND currency_name = ? AND available = ?";
        int rowN = data.update(sql, amount, amount, userId, currencyName, available);
        if (rowN < 1)
            throw new RuntimeException("concurrent error");
    }

    /**
     * 取消提币申请
     */
    @Transactional(rollbackFor = Throwable.class)
    public void remove(long userId, long id) {
        String sql = "SELECT currency_name, amount, fee, state FROM accounting_withdraw WHERE user_id = ? AND id = ?";
        DataMap withdraw = data.queryOne(sql, userId, id);
        String currencyName = withdraw.getString("currency_name");
        BigDecimal amount = withdraw.getBig("amount");
        BigDecimal fee = withdraw.getBig("fee");
        int state = withdraw.getInt("state");
        if (state != State.WITHDRAW_CREATED)
            throw new RuntimeException("invalid state");

        BigDecimal total = amount.add(fee);

        sql = "UPDATE accounting_balance SET available = available + ?, withdrawing = withdrawing - ? WHERE user_id = ? AND currency_name = ?";
        data.update(sql, total, total, userId, currencyName);

        sql = "UPDATE accounting_withdraw SET state = ? WHERE user_id = ? AND id = ? AND state = ?";
        int rowN = data.update(sql, State.WITHDRAW_CANCELED, userId, id, State.WITHDRAW_CREATED);
        if (rowN < 1)
            throw new RuntimeException("concurrent error");
    }

    /**
     * 近期提币列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> list(long userId) {
        String sql = "SELECT id, chain_name, currency_name, to_address, amount, txid, confirm, create_time, update_time, state FROM accounting_withdraw WHERE user_id = ? ORDER BY id DESC";
        List<DataMap> withdraws = data.query(sql, userId);
        return withdraws;
    }

    /**
     * 查询某地址最近一笔提币记录
     */
    public DataMap latest(long userId, String chainName, String currencyName, String toAddress, String memo) {
        DataMap latest;
        if (memo == null) {
            String sql = "SELECT id, txid, confirm FROM accounting_withdraw WHERE user_id = ? AND chain_name = ? AND currency_name = ? AND to_address = ? AND state >= ? LIMIT 1";
            latest = data.queryOne(sql, userId, chainName, currencyName, toAddress, State.WITHDRAW_REVIEWED2);
        } else {
            String sql = "SELECT id, txid, confirm FROM accounting_withdraw WHERE user_id = ? AND chain_name = ? AND currency_name = ? AND to_address = ? AND memo = ? AND state >= ? LIMIT 1";
            latest = data.queryOne(sql, userId, chainName, currencyName, toAddress, memo, State.WITHDRAW_REVIEWED2);
        }
        return latest;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Async
    public void check() {
        String sql = "SELECT id, user_id, chain_name, currency_name FROM accounting_withdraw WHERE state = ? LIMIT ?";
        List<DataMap> withdraws = data.query(sql, State.WITHDRAW_REVIEWED2, 100);

        for (DataMap withdraw : withdraws) {
            long id = withdraw.getLong("id");
            long userId = withdraw.getLong("user_id");
            String chainName = withdraw.getString("chain_name");
            String currencyName = withdraw.getString("currency_name");

            String walletId = DigestUtils.sha256Hex(walletConfig.getKey() + "0" + id + currencyName);

            JSONObject transaction = http.post("/transaction", "id", walletId, "chain", chainName, "currency", currencyName);
            try {
                String txid = transaction.getString("chain_txid");
                Integer confirm = transaction.getIntValue("chain_confirm");
                Integer state = transaction.getInteger("state");

                int newState = state == 4 ? State.WITHDRAW_BROADCASTED : State.WITHDRAW_REVIEWED2;

                if (txid != null) {
                    sql = "UPDATE accounting_withdraw SET txid = ?, confirm = ?, state = ? WHERE id = ? AND user_id = ?";
                    data.update(sql, txid, confirm, newState, id, userId);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
