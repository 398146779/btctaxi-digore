package com.btctaxi.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.accounting.config.RedisKeyConfig;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SymbolService {
    private Data data;
    private StringRedisTemplate kv;

    public SymbolService(Data data, StringRedisTemplate kv) {
        this.data = data;
        this.kv = kv;
    }

    /**
     * Symbol列表
     */
    @Transactional(readOnly = true)
    public JSONArray list() {
        String sql = "SELECT currency_name FROM accounting_currency GROUP BY currency_name";
        List<DataMap> symbols = data.query(sql);
        JSONArray json = new JSONArray();
        symbols.forEach(symbol -> json.add(query(symbol.getString("currency_name"))));
        return json;
    }

    /**
     * 查询一条Symbol
     */
    public JSONObject query(String currencyName) {
        String key = RedisKeyConfig.SYMBOL_PREFIX + currencyName;
        JSONObject json = new JSONObject();

        try {
            Map<String, String> symbol = kv.<String, String>opsForHash().entries(key);
            int scale = Integer.parseInt(symbol.get("scale"));
            String fullName = symbol.get("full_name");
            json.put("currency_name", currencyName);
            json.put("scale", scale);
            json.put("full_name", fullName);
        }catch (Exception e){
            log.error(String.format("查询一条Symbol error key = %s", key),e);
        }
        return json;
    }
}
