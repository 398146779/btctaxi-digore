package com.btctaxi.service;

import com.alibaba.fastjson.JSONObject;
import com.btctaxi.common.Data;
import com.btctaxi.common.DataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BalanceService {
    private Data data;
    private SymbolService symbolService;

    public BalanceService(Data data, SymbolService symbolService) {
        this.data = data;
        this.symbolService = symbolService;
    }

    /**
     * 查询用户各币种余额列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> list(long userId) {
        //查询余额
        String sql = "SELECT currency_name, available, withdrawing, ordering, locking FROM accounting_balance WHERE user_id = ?";
        List<DataMap> balances = data.query(sql, userId);

        if (!balances.isEmpty()) {
            //委托中
            sql = "SELECT direction, pair_name, price, unclosed FROM tb_order_active WHERE user_id = ? AND price > 0 AND unclosed > 0";
            List<DataMap> orders = data.query(sql, userId);
            Map<String, BigDecimal> locks = new HashMap<>();
            orders.forEach(order ->
            {
                String pairName = order.getString("pair_name");
                String[] names = pairName.split("_");
                int direction = order.getInt("direction");
                BigDecimal price = order.getBig("price");
                BigDecimal unclosed = order.getBig("unclosed");
                BigDecimal locked = direction == 1 ? price.multiply(unclosed) : unclosed;
                String key = direction == 1 ? names[1] : names[0];
                BigDecimal val = locks.get(key);
                locks.put(key, val == null ? locked : val.add(locked));
            });

            //未确认的
            Map<String, BigDecimal> confirmings = new HashMap<>();
            sql = "SELECT chain_name, currency_name, amount, confirm FROM accounting_deposit WHERE user_id = ? AND state = 0";
            List<DataMap> deposits = data.query(sql, userId);

            deposits.forEach(in -> {
                String chainName = in.getString("chain_name");
                String currencyName = in.getString("currency_name");
                int confirm = in.getInt("confirm");

                String currencySql = "SELECT exchange_confirm, withdraw_confirm FROM accounting_currency WHERE chain_name = ? AND currency_name = ?";
                DataMap currency = data.queryOne(currencySql, chainName, currencyName);
                int exchangeConfirm = currency.getInt("exchange_confirm");
                int withdrawConfirm = currency.getInt("withdraw_confirm");

                if (confirm >= exchangeConfirm && confirm < withdrawConfirm) {
                    BigDecimal amount = in.getBig("amount");
                    BigDecimal prev = confirmings.get(currencyName);
                    if (prev == null)
                        confirmings.put(currencyName, amount);
                    else
                        confirmings.put(currencyName, prev.add(amount));
                }
            });

            //查询币种配置信息
            for (DataMap balance : balances) {
                String currencyName = balance.getString("currency_name");
                JSONObject symbol = symbolService.query(currencyName);

                BigDecimal ordering = locks.get(currencyName);
                ordering = ordering == null ? BigDecimal.ZERO : ordering;
                balance.put("ordering", ordering);

                BigDecimal confirming = confirmings.get(currencyName);
                balance.put("confirming", confirming == null ? BigDecimal.ZERO : confirming);
                balance.putAll(symbol);
            }
        }


        return balances;
    }

    /**
     * 查询某一币种余额信息
     */
    @Transactional(readOnly = true)
    public DataMap query(long userId, String currencyName) {
        String sql = "SELECT currency_name, available, withdrawing, ordering, locking FROM accounting_balance WHERE user_id = ? AND currency_name = ?";
        DataMap balance = data.queryOne(sql, userId, currencyName);
        JSONObject symbol = symbolService.query(currencyName);

        if (balance == null) {
            balance = new DataMap();
            balance.put("currency_name", currencyName);
            balance.put("available", BigDecimal.ZERO);
            balance.put("withdrawing", BigDecimal.ZERO);
            balance.put("ordering", BigDecimal.ZERO);
            balance.put("locking", BigDecimal.ZERO);
        }

        //委托中
        sql = "SELECT direction, pair_name, price, unclosed FROM tb_order_active WHERE user_id = ? AND price > 0 AND unclosed > 0";
        List<DataMap> orders = data.query(sql, userId);
        Map<String, BigDecimal> locks = new HashMap<>();
        orders.forEach(order ->
        {
            String pairName = order.getString("pair_name");
            String[] names = pairName.split("_");
            int direction = order.getInt("direction");
            BigDecimal price = order.getBig("price");
            BigDecimal unclosed = order.getBig("unclosed");
            BigDecimal locked = direction == 1 ? price.multiply(unclosed) : unclosed;
            String key = direction == 1 ? names[1] : names[0];
            BigDecimal val = locks.get(key);
            locks.put(key, val == null ? locked : val.add(locked));
        });

        BigDecimal ordering = locks.get(currencyName);
        ordering = ordering == null ? BigDecimal.ZERO : ordering;
        balance.put("ordering", ordering);

        //未确认的
        BigDecimal confirming = BigDecimal.ZERO;
        sql = "SELECT chain_name, amount, confirm FROM accounting_deposit WHERE user_id = ? AND currency_name = ? AND state = 0";
        List<DataMap> deposits = data.query(sql, userId, currencyName);

        for (DataMap deposit : deposits) {
            String chainName = deposit.getString("chain_name");
            int confirm = deposit.getInt("confirm");

            String currencySql = "SELECT exchange_confirm, withdraw_confirm FROM accounting_currency WHERE chain_name = ? AND currency_name = ?";
            DataMap currency = data.queryOne(currencySql, chainName, currencyName);
            int exchangeConfirm = currency.getInt("exchange_confirm");
            int withdrawConfirm = currency.getInt("withdraw_confirm");

            if (confirm >= exchangeConfirm && confirm < withdrawConfirm) {
                BigDecimal amount = deposit.getBig("amount");
                confirming = confirming.add(amount);
            }
        }
        balance.put("confirming", confirming);
        balance.putAll(symbol);

        return balance;
    }

    /**
     * 查询用户各币种锁定列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> listLocking(long userId) {
        //查询余额
        String sql = "SELECT currency_name, release_time, amount FROM accounting_balance_lock WHERE user_id = ?";
        List<DataMap> lockings = data.query(sql, userId);

        return lockings;
    }
}
