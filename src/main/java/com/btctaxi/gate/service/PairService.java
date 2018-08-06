package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.error.ServiceError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PairService extends BaseService {
    private AccountingService accountingService;
    private MarketService marketService;

    public PairService(AccountingService accountingService, MarketService marketService) {
        this.accountingService = accountingService;
        this.marketService = marketService;
    }

    @Transactional(readOnly = true)
    public List<DataMap> list(String currencyName) {
        ArrayList<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT name, product_name, currency_name, amount_scale, price_scale, min_amount, max_amount, min_total, max_total FROM tb_pair WHERE state = 'ONLINE'");
        if (currencyName != null) {
            sql.append(" AND currency_name = ?");
            params.add(currencyName);
        }
        List<DataMap> pairs = data.query(sql.toString(), params.toArray());

        //获取Symbol信息
        JSONArray json = accountingService.post("/symbol/list");
        Map<String, Integer> scales = new HashMap<>();
        for (int i = 0; i < json.size(); i++) {
            JSONObject o = json.getJSONObject(i);
            String key = o.getString("currency_name");
            int value = o.getIntValue("scale");
            scales.put(key, value);
        }

        //添加scale
        for (DataMap pair : pairs) {
            String pName = pair.getString("product_name");
            String cName = pair.getString("currency_name");
            int pScale = scales.get(pName);
            int cScale = scales.get(cName);
            pair.put("product_scale", pScale);
            pair.put("currency_scale", cScale);
        }

        return pairs;
    }

    @Transactional(readOnly = true)
    public DataMap query(String pairName) {
        String sql = "SELECT name, product_name, currency_name, amount_scale, price_scale FROM tb_pair WHERE name = ? AND state = 'ONLINE'";
        DataMap pair = data.queryOne(sql, pairName);
        if (pair == null)
            throw new ServiceError(ServiceError.PAIR_NOT_EXISTS);

        String pName = pair.getString("product_name");
        String cName = pair.getString("currency_name");

        JSONObject pJson = accountingService.post("/symbol/query", "currency_name", pName);
        JSONObject cJson = accountingService.post("/symbol/query", "currency_name", cName);

        int pScale = pJson.getIntValue("scale");
        int cScale = cJson.getIntValue("scale");
        pair.put("product_scale", pScale);
        pair.put("currency_scale", cScale);

        return pair;
    }

    @Transactional(readOnly = true)
    public List<DataMap> recommends() {
        String sql = "SELECT name, product_name, currency_name FROM tb_pair WHERE state = 'ONLINE' AND recommended = TRUE";
        List<DataMap> pairs = data.query(sql);
        return pairs;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> group(long userId) {
        String sql = "SELECT name, product_name, currency_name FROM tb_pair WHERE state = 'ONLINE'";
        List<DataMap> pairs = data.query(sql);

        sql = "SELECT pair_name FROM tb_favorite_pair where uid = ?";
        List<DataMap> favorites = data.query(sql, userId);

        Map<String, List<DataMap>> group = new HashMap<>();
        pairs.forEach(pair ->
        {
            String currencyName = pair.getString("currency_name");
            if (favorites != null && !favorites.isEmpty()) {
                favorites.forEach(f ->
                {
                    if (f.getString("pair_name").equals(pair.getString("name")))
                        pair.put("favorite", true);
                });
            }
            group.computeIfAbsent(currencyName, items -> new ArrayList<>()).add(pair);
        });
        List<Map<String, Object>> result = new ArrayList<>();
        group.forEach((k, v) ->
        {
            Map<String, Object> map = new HashMap<>();
            map.put("currency_name", k);
            map.put("pairs", v);
            result.add(map);
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<DataMap> havePair(Long userId, String pair) {
        String sql = "SELECT pair_name FROM tb_favorite_pair where uid = ? and pair_name=?";
        List<DataMap> favorites = data.query(sql, userId, pair);
        return favorites;
    }

    /**
     * 搜索交易对
     *
     * @param userId      用户id
     * @param search_text 搜索内容
     * @return 搜索结果列表
     */
    @Transactional(readOnly = true)
    public List<DataMap> search(String search_text) {
        String sql = "SELECT name, product_name, currency_name FROM tb_pair WHERE state = 'ONLINE' AND name like ?";
        List<DataMap> searchList = data.query(sql, "%" + search_text + "%");

        return searchList;
    }
}
