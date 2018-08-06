package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.error.ServiceError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class AddressService extends BaseService {
    private AccountingService accountingService;

    public AddressService(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    public JSONObject depositQuery(long userId, String chainName, String currencyName) {
        JSONObject json = accountingService.post("/address/query", "user_id", userId, "chain_name", chainName, "currency_name", currencyName);
        return json;
    }

    @Transactional(readOnly = true)
    public List<DataMap> withdrawList(long userId, String chainName, String currencyName) {
        String sql = "SELECT id, chain_name, currency_name, label, address, memo FROM tb_favorite_address WHERE uid = ? AND chain_name = ? AND currency_name = ?";
        List<DataMap> addrs = data.query(sql, userId, chainName, currencyName);
        return addrs;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataMap create(long userId, String chainName, String currencyName, String label, String address, String memo) {
        if (label == null || label.length() > 32)
            throw new ServiceError(HttpServletResponse.SC_BAD_REQUEST);
        JSONArray currencies = accountingService.post("/currency/query", "currency_name", currencyName);
        boolean found = false;
        for (int i = 0; i < currencies.size(); i++) {
            JSONObject o = currencies.getJSONObject(i);
            String c = o.getString("chain_name");
            if (c.equals(chainName)) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new ServiceError(ServiceError.WITHDRAW_ADDRESS_CREATE_CURRENCY_NOT_EXISTS);

        String sql = "INSERT INTO tb_favorite_address(uid, chain_name, currency_name, label, address, memo) VALUES (?, ?, ?, ?, ?, ?)";
        long id = data.insert(sql, userId, chainName, currencyName, label, address, memo);

        DataMap map = new DataMap();
        map.put("id", id);
        map.put("chain_name", chainName);
        map.put("currency_name", currencyName);
        map.put("label", label);
        map.put("address", address);
        map.put("memo", memo);
        return map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void remove(long userId, long id) {
        String sql = "DELETE FROM tb_favorite_address WHERE id = ? AND uid = ?";
        data.update(sql, id, userId);
    }
}
