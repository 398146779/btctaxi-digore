package com.btctaxi.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DepositService {
    private Data data;
    private HttpService http;

    public DepositService(Data data, HttpService http) {
        this.data = data;
        this.http = http;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Async
    public void check(Long maxId, Integer size) {
        //取出已成功的最大ID
        if (maxId == null) {
            String sql = "SELECT MIN(id) AS id FROM accounting_deposit WHERE state = 0";
            DataMap latest = data.queryOne(sql);
            if (latest == null || latest.getLong("id") == null) {
                sql = "SELECT MAX(id) AS id FROM accounting_deposit WHERE state = 1";
                latest = data.queryOne(sql);
            }
            Optional<Long> optionalId = latest == null ? Optional.of(0L) : Optional.ofNullable(latest.getLong("id"));
            maxId = optionalId.orElse(0L);
        }
        if (size == null)
            size = 100;

        //检查状态不成功或confirm数不到最终确认数的id
        JSONArray transactions = http.post("/transaction/in", "id", maxId, "size", size);

        log.info("/transaction/in, {}, {}", maxId, size);

        for (int i = 0; i < transactions.size(); i++) {
            JSONObject json = transactions.getJSONObject(i);
            log.info(json.toJSONString());

            long id = json.getLongValue("id");
            int txtype = json.getIntValue("txtype");
            String toAddress = json.getString("to_address");
            String memo = json.getString("memo");
            String chainName = json.getString("chain");
            String currencyName = json.getString("currency");
            String txid = json.getString("chain_txid");
            int confirm = json.getIntValue("chain_confirm");
            int state = json.getIntValue("state");
            BigDecimal amount = json.getBigDecimal("amount");

            if (txtype != 0) {
                log.info("miner fee deposit");
                continue;
            }

            String sql = "SELECT exchange_confirm, min_deposit_amount, memo_support FROM accounting_currency WHERE chain_name = ? AND currency_name = ? AND depositable = TRUE";
            DataMap currency = data.queryOne(sql, chainName, currencyName);
            if (currency == null) {
                log.error("currency unavailable: chain_name {} currency_name {} amount {}", chainName, currencyName, amount.toPlainString());
                continue;
            }
            int exchangeConfirm = currency.getInt("exchange_confirm");
            BigDecimal minDepositAmount = currency.getBig("min_deposit_amount");
            boolean memoSupport = currency.getBoolean("memo_support");

            if (amount.compareTo(minDepositAmount) < 0) {
                log.error("min deposit amount: chain_name {} currency_name {} amount {} txid {}", chainName, currencyName, amount.toPlainString(), txid);
                continue;
            }

            //取得用户ID
            sql = "SELECT user_id FROM accounting_deposit_address WHERE chain_name = ? AND address = ? LIMIT 1";
            DataMap userAddress = data.queryOne(sql, chainName, memoSupport ? memo : toAddress);
            if (userAddress == null) {
                log.error("user address not found: chain_name {} currency_name {} address {} memo {}", chainName, currencyName, toAddress, memo);
                continue;
            }
            long userId = userAddress.getLong("user_id");
            sql = "SELECT confirm, state FROM accounting_deposit WHERE id = ? AND user_id = ?";
            DataMap deposit = data.queryOne(sql, id, userId);
            int prevConfirm = deposit == null ? 0 : deposit.getInt("confirm");
            int prevState = deposit == null ? 0 : deposit.getInt("state");

            sql = "INSERT INTO accounting_deposit(id, user_id, chain_name, currency_name, to_address, memo, amount, txid, confirm, state) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE confirm = ?, state = ?";
            data.update(sql, id, userId, chainName, currencyName, toAddress, memo, amount, txid, confirm, state, confirm, state);

            if (prevConfirm < exchangeConfirm && confirm >= exchangeConfirm) {
                //TODO 三方平台验证转账合法性
                log.info("depositing: available {}", amount.toPlainString());
                sql = "INSERT INTO accounting_balance(user_id, currency_name, available) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE available = available + ?";
                int rowN = data.update(sql, userId, currencyName, amount, amount);
                if (rowN < 1) {
                    log.error("balance concurrent access");
                    throw new RuntimeException("balance concurrent access");
                }
            }
        }
    }

    /**
     * 充币列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> list(long userId) {
        String sql = "SELECT id, chain_name, currency_name, to_address, amount, txid, confirm, state, create_time, update_time FROM accounting_deposit WHERE user_id = ? ORDER BY id DESC";
        List<DataMap> deposits = data.query(sql, userId);
        return deposits;
    }
}
