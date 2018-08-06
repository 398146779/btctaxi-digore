package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import genesis.gate.service.AccountingService;
import genesis.gate.service.CurrencyService;
import genesis.gate.service.RefundService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/gate/statistics")
public class StatisticsController extends BaseController {

    private CurrencyService currencyService;
    private AccountingService accountingService;
    private RefundService refundService;

    public StatisticsController(CurrencyService currencyService, AccountingService accountingService, RefundService refundService) {
        this.currencyService = currencyService;
        this.accountingService = accountingService;
        this.refundService = refundService;
    }

    @RequestMapping("/tb")
    @Any
    public Map<String, Object> tb() {
        // 总流通量获取
        String tbName = currencyService.getTbCurrencyName();
        BigDecimal tbTotal = accountingService.getTotalAvailable(tbName);

        Map<String, Object> total_values = new HashMap<>();
        total_values.put("unit", "BTC");
        total_values.put("amount", "0");

        if (tbTotal != null && tbTotal.compareTo(BigDecimal.ZERO) > 0) {
            // 总市值计算
            JSONObject tbRate = currencyService.ratesToBtc(); // 汇率列表
            if (tbRate.containsKey(tbName)) {
                BigDecimal rate = tbRate.getBigDecimal(tbName); // 需要的汇率
                total_values.put("amount", tbTotal.multiply(rate).setScale(4, BigDecimal.ROUND_DOWN));
            }
        }

        // 整合数据返回
        Map<String, Object> map = new HashMap<>();
        map.put("total_available", tbTotal); // 总流通量
        map.put("total_value", total_values); // 总市值

        return map;
    }

    @RequestMapping("/refund")
    @Any
    public Map<String, Object> refund() {
        Map<String, Object> yesTotal = refundService.getYesterdayMineAndProfitTotal();

        // 整合数据返回
        Map<String, Object> map = new HashMap<>();
        map.put("yesterday_mine_amount", new HashMap<String, Object>() {{  // 昨天挖矿数量
            put("amount", new BigDecimal((String) yesTotal.get("mine_amount_total")));
            put("unit", yesTotal.get("mine_unit"));
        }});

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("day", yesTotal.get("yesterday").toString());
        JSONObject avgRates = (JSONObject) accountingService.postMap("gate/commission/avgChangeRate", params);

        BigDecimal yesRefundTotal = new BigDecimal((String) yesTotal.get("refund_profit_total"));
        BigDecimal rate = Optional.ofNullable(avgRates.getBigDecimal(yesTotal.get("refund_profit_unit").toString())).orElse(BigDecimal.ZERO); // 需要的汇率
        map.put("yesterday_refund_profit", new HashMap<String, Object>() {{ // 昨日已分配收入
            put("unit", "BTC");
            put("amount", yesRefundTotal.multiply(rate).setScale(4, BigDecimal.ROUND_DOWN));
        }});
        return map;
    }


}
