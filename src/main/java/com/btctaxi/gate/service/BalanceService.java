package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BalanceService extends BaseService {
    private final String KEY_PRICE_PREFIX = "PRICE_";
    private AccountingService accountingService;
    private CurrencyService currencyService;

    public BalanceService(AccountingService accountingService, CurrencyService currencyService) {
        this.accountingService = accountingService;
        this.currencyService = currencyService;
    }

    @Transactional(readOnly = true)
    public JSONArray list(long userId) {
        JSONArray balances = accountingService.post("/balance/list", "user_id", userId);

        JSONObject quota = accountingService.post("/withdraw/quota/query", "user_id", userId);
        BigDecimal quotaBTC = quota.getBigDecimal("amount");

        for (int i = 0; i < balances.size(); i++) {
            JSONObject balance = balances.getJSONObject(i);
            String currencyName = balance.getString("currency_name");
            int scale = balance.getIntValue("scale");

            BigDecimal available = balance.getBigDecimal("available");
            BigDecimal withdrawing = balance.getBigDecimal("withdrawing");
            BigDecimal confirming = balance.getBigDecimal("confirming");
            BigDecimal locking = balance.getBigDecimal("locking");
            BigDecimal ordering = balance.getBigDecimal("ordering");
            BigDecimal unavailable = ordering.add(withdrawing).add(locking);

            balance.put("unavailable", unavailable);
            //等值BTC
            BigDecimal rate;
            if (!"BTC".equals(currencyName)) {
                JSONObject json = currencyService.ratesToBtc();
                rate = json.getBigDecimal(currencyName);
            } else {
                rate = BigDecimal.ONE;
            }

            boolean priced = rate != null;
            balance.put("priced", priced);

            rate = rate == null ? BigDecimal.ZERO : rate;

            BigDecimal equality = available.add(unavailable).multiply(rate).setScale(scale, BigDecimal.ROUND_DOWN);
            balance.put("equality", equality);

            BigDecimal withdrawable = available.subtract(confirming);
            BigDecimal quotaAmount = rate.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : quotaBTC.divide(rate, scale, BigDecimal.ROUND_DOWN);
            balance.put("withdrawable_amount", withdrawable.min(quotaAmount));
            balance.put("quota", quotaAmount);
            balance.put("quota_btc", quotaBTC);
        }
        return balances;
    }

    @Transactional(rollbackFor = Throwable.class)
    public JSONObject query(long userId, String currencyName) {
        JSONObject balance = accountingService.post("/balance/query", "user_id", userId, "currency_name", currencyName);

        JSONObject quota = accountingService.post("/withdraw/quota/query", "user_id", userId);
        BigDecimal quotaBTC = quota.getBigDecimal("amount");

        int scale = balance.getIntValue("scale");
        BigDecimal available = balance.getBigDecimal("available");
        BigDecimal withdrawing = balance.getBigDecimal("withdrawing");
        BigDecimal confirming = balance.getBigDecimal("confirming");
        BigDecimal locking = balance.getBigDecimal("locking");
        BigDecimal ordering = balance.getBigDecimal("ordering");
        BigDecimal unavailable = ordering.add(withdrawing).add(locking);

        balance.put("unavailable", unavailable);
        //等值BTC
        BigDecimal rate;
        if (!"BTC".equals(currencyName)) {
            JSONObject json = currencyService.ratesToBtc();
            rate = json.getBigDecimal(currencyName);
        } else {
            rate = BigDecimal.ONE;
        }

        boolean priced = rate != null;
        balance.put("priced", priced);

        rate = rate == null ? BigDecimal.ZERO : rate;

        BigDecimal equality = available.add(unavailable).multiply(rate).setScale(scale, BigDecimal.ROUND_DOWN);
        balance.put("equality", equality);

        BigDecimal withdrawable = available.subtract(confirming);
        BigDecimal quotaAmount = rate.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : quotaBTC.divide(rate, scale, BigDecimal.ROUND_DOWN);
        balance.put("withdrawable_amount", withdrawable.min(quotaAmount));
        balance.put("quota", quotaAmount);
        balance.put("quota_btc", quotaBTC);

        return balance;
    }
}
