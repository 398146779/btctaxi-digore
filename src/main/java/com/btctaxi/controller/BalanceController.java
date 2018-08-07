package com.btctaxi.controller;

import com.btctaxi.service.BalanceService;
import com.btctaxi.common.DataMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/balance")
public class BalanceController {
    private BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * 币种余额列表
     */
    @RequestMapping("/list")
    public List<DataMap> list(@RequestParam("user_id") Long userId) {
        return balanceService.list(userId);
    }

    /**
     * 查询币种余额
     */
    @RequestMapping("/query")
    public DataMap query(@RequestParam("user_id") Long userId, @RequestParam("currency_name") String currencyName) {
        return balanceService.query(userId, currencyName);
    }

    /**
     * 查询锁仓信息
     */
    @RequestMapping("/locking/list")
    public List<DataMap> listLocking(@RequestParam("user_id") Long userId) {
        return balanceService.listLocking(userId);
    }
}
