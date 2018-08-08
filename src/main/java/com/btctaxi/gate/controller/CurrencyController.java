package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONArray;
import com.btctaxi.gate.service.CurrencyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/currency")
public class CurrencyController extends BaseController {
    protected CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @RequestMapping("/list")
    @Any
    public Map<String, Object> list() {
        JSONArray currencies = currencyService.list();
        Map<String, Object> map = new HashMap<>();
        map.put("items", currencies);
        return map;
    }

    @RequestMapping("/query")
    @Any
    public Map<String, Object> query(@RequestParam("currency_name") String currencyName) {
        JSONArray currencies = currencyService.query(currencyName);
        Map<String, Object> map = new HashMap<>();
        map.put("items", currencies);
        return map;
    }

    @RequestMapping("/rates")
    @Any
    public Map<String, Object> rates(@RequestParam("currency_name") String currencyName) {
        return currencyService.rates(currencyName);
    }

    @RequestMapping("/rate")
    @Any
    public Map<String, Object> rate(@RequestParam String name) {
        return currencyService.rates(name);
    }
}
