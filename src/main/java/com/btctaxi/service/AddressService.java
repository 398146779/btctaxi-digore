package com.btctaxi.service;

import com.alibaba.fastjson.JSONObject;
import genesis.accounting.config.WalletConfig;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AddressService {
    private Data data;
    private WalletConfig walletConfig;
    private HttpService httpService;

    public AddressService(Data data, WalletConfig walletConfig, HttpService httpService) {
        this.data = data;
        this.walletConfig = walletConfig;
        this.httpService = httpService;
    }

    /**
     * 取得用户地址
     */
    @Transactional(rollbackFor = Throwable.class)
    public DataMap query(long userId, String chainName, String currencyName) {
        String sql = "SELECT memo_support FROM accounting_currency WHERE chain_name = ? AND currency_name = ?";
        DataMap currency = data.queryOne(sql, chainName, currencyName);
        boolean memoSupport = currency.getBoolean("memo_support");

        String address, memo = null;

        sql = "SELECT address FROM accounting_deposit_address WHERE user_id = ? AND chain_name = ? AND currency_name = ?";
        DataMap userAddress = data.queryOne(sql, userId, chainName, currencyName);
        if (userAddress != null) {
            address = userAddress.getString("address");
        } else {
            String id = DigestUtils.sha256Hex(walletConfig.getKey() + userId + chainName);
            JSONObject json = httpService.post("/address/create", "id", id, "chain", chainName);
            address = json.getString("address");
            if (address == null)
                throw new RuntimeException("create address error");

            sql = "INSERT INTO accounting_deposit_address(user_id, chain_name, currency_name, address) VALUES(?, ?, ?, ?)";
            data.update(sql, userId, chainName, currencyName, address);
        }

        if (memoSupport) {
            memo = address;
            sql = "SELECT address FROM accounting_internal_address WHERE chain_name = ? LIMIT 1";
            DataMap internalAddress = data.queryOne(sql, chainName);
            address = internalAddress.getString("address");
        }

        DataMap result = new DataMap();
        result.put("address", address);
        result.put("memo", memo);

        return result;
    }
    /**
     * 取得用户地址
     */
    @Transactional(rollbackFor = Throwable.class)
    public DataMap queryUserId(String chainName, String address) {
        String sql = "SELECT user_id FROM accounting_deposit_address WHERE chain_name = ? AND address = ? LIMIT 1";


        return data.queryOne(sql, chainName, address);
    }
}
