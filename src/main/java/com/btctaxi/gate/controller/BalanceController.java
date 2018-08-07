package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.btctaxi.gate.service.BalanceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/balance")
public class BalanceController extends BaseController {
    private BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * 查询个人资产列表
     */
    @RequestMapping("/list")
    public Map<String, Object> list() {
        JSONArray balances = balanceService.list(sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", balances);
        return map;
    }

    /**
     * 查询币种余额等信息
     */
    @RequestMapping("/query")
    public JSONObject query(@RequestParam("currency_name") String currencyName) {
        return balanceService.query(sess.getId(), currencyName);
    }
}
