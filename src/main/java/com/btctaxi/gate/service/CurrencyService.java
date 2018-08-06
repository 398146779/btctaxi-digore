package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.gate.config.DistConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 币种
 */
@Service
public class CurrencyService extends BaseService {
    private final String[] CURRENCIES = {"GBP", "EUR", "CNY", "JPY", "HKD", "TWD", "SGD", "KRW"};

    private AccountingService accountingService;
    private DistConfig distConfig;

    public CurrencyService(AccountingService accountingService, DistConfig distConfig) {
        this.accountingService = accountingService;
        this.distConfig = distConfig;
    }

    /**
     * 币种列表
     */
    public JSONArray list() {
        JSONArray currencies = accountingService.post("/currency/list");
        return currencies;
    }

    /**
     * 查看币种详情
     */
    public JSONArray query(String currencyName) {
        JSONArray currencies = accountingService.post("/currency/query", "currency_name", currencyName);
        return currencies;
    }

    /**
     * 汇率
     */
    public Map<String, Object> rates(String currencyName) {
        ResponseEntity<String> res = http.getForEntity(distConfig.getRateUrl(), String.class);
        JSONObject rates = JSON.parseObject(res.getBody());
        rates.put("BTC", BigDecimal.ONE); //防止BTC不存在
        BigDecimal rate1 = rates.getBigDecimal(currencyName);
        BigDecimal rate2 = rates.getBigDecimal("USDT");
        rate2 = rate2 == null ? BigDecimal.ONE : rate2; //防止USDT未配置的情况

        BigDecimal finalRate = rate1.divide(rate2, 2, BigDecimal.ROUND_DOWN);
        Map<String, Object> map = new HashMap<>();
        for (String c : CURRENCIES) {
            String r = kv.opsForValue().get("PRICE_USD_" + c);
            BigDecimal x = new BigDecimal(r);
            map.put(c, finalRate.multiply(x).setScale(2, BigDecimal.ROUND_DOWN));
        }
        map.put("USD", finalRate.setScale(2, BigDecimal.ROUND_DOWN));
        return map;
    }

    /**
     * 各币对BTC汇率
     */
    /**
     * 各币对BTC汇率
     */
    public JSONObject ratesToBtc() {
        ResponseEntity<String> res = http.getForEntity(distConfig.getRateUrl(), String.class);
        JSONObject rates = JSON.parseObject(res.getBody());
        rates.put("BTC", BigDecimal.ONE); //防止BTC不存在

        return rates;
    }

    /**
     * 获取TB平台币名称
     */
    public String getTbCurrencyName() {
        return kv.<String, String>opsForHash().get("COMMON_CONFIG", "THINK_BIT");
    }
}
