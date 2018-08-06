package com.btctaxi.service;

import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CurrencyService {
    private Data data;

    public CurrencyService(Data data) {
        this.data = data;
    }

    /**
     * 币种列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> list() {
        String sql = "SELECT chain_name, currency_name, full_name, scale, exchange_confirm, withdraw_confirm, min_deposit_amount, min_withdraw_amount, min_review_amount, withdraw_fee, depositable, withdrawable, memo_support, scan_url FROM accounting_currency";
        List<DataMap> currencies = data.query(sql);
        return currencies;
    }

    /**
     * 币种查询
     */
    @Transactional(readOnly = true)
    public List<DataMap> query(String currencyName) {
        String sql = "SELECT chain_name, currency_name, full_name, scale, exchange_confirm, withdraw_confirm, min_deposit_amount, min_withdraw_amount, withdraw_fee, depositable, withdrawable, memo_support, scan_url FROM accounting_currency WHERE currency_name = ?";
        List<DataMap> currencyList = data.query(sql, currencyName);
        return currencyList;
    }
}
