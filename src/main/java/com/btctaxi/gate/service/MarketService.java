package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 大盘行情
 */
@Service
public class MarketService extends BaseService {
    private final String TICKER_API = "https://api.thinkbit.com/v2/market/ticker";

    public MarketService() {
    }


    /**
     * 大盘报价数据
     *
     * @param pairs 指定交易对，以逗号分割
     */
    public List<JSONObject> ticker(String pairs) {

        String url = TICKER_API;
        if (StringUtils.isNotEmpty(pairs)) {
            url = url + "?pairs=" + pairs;
        }
        List<JSONObject> list = Lists.newArrayList();

        try {
            ResponseEntity<String> res = http.getForEntity(url, String.class);
            JSONObject rates = JSON.parseObject(res.getBody());

            JSONArray ja = rates.getJSONArray("data");
            list = ja.stream().filter(Objects::nonNull).map(e -> (JSONObject) e).collect(Collectors.toList());
            if (list.isEmpty()) {
                log.info(String.format("ticker warn: body = %s, pairs = %s, url=%s", res.getBody(), pairs, url));
            }
        } catch (RestClientException e) {
            log.info(String.format("ticker error: list.size() = %s, pairs = %s, url=%s", list.size(), pairs, url), e);
        }

        return list;
    }

    public List<JSONObject> tickerAll() {
        List<JSONObject> apiRes = ticker("");
        if (apiRes.isEmpty()) {
            //from redis cache 防止数据为空
            String cacheString = kv.opsForValue().get("ticker_all_data");
            JSONArray cacheRes = JSON.parseArray(cacheString);
            if (cacheRes.isEmpty()) {
                return Lists.newArrayList();
            }
            log.info("ticker all from cache");
            return cacheRes.stream().filter(Objects::nonNull).map(e -> (JSONObject) e).collect(Collectors.toList());
        } else {
            Random rand = new Random();
            if (rand.nextInt(5) == 1) {
                kv.opsForValue().set("ticker_all_data", JSONArray.toJSONString(apiRes));
                log.info("ticker all set to cache");
            }
        }

        return apiRes;
    }

    /**
     * 获取24小时交易榜
     */
    public List<JSONObject> topGainers(List<JSONObject> tickersAll, int limit) {
        List<JSONObject> res = tickersAll.stream()
                .sorted((a, b) -> b.getBigDecimal("change_24h").compareTo(a.getBigDecimal("change_24h")))
                .limit(limit)
                .collect(Collectors.toList());

        return res;
    }

    /**
     * 获取24小时交易榜
     */
    public List<JSONObject> topHots(List<JSONObject> tickersAll, int limit) {
        List<JSONObject> res = tickersAll.stream()
                .sorted((a, b) -> b.getBigDecimal("volume_24h").compareTo(a.getBigDecimal("volume_24h")))
                .limit(limit)
                .peek(o -> o.put("name", o.getString("pair")))
                .collect(Collectors.toList());

        return res;
    }

    public Map<String, String> defaultTickerData() {
        Map<String, String> map = new HashMap<>();
        map.put("change_24h", "0");
        map.put("increment_24h", "0");
        map.put("highest_24h", "0");
        map.put("lowest_24h", "0");
        map.put("highest_bid", "0");
        map.put("lowest_ask_amount", "0");
        map.put("current", "0");
        map.put("volume_current", "0");
        map.put("volume_24h", "0");
        map.put("lowest_ask", "0");
        map.put("highest_bid_amount", "0");
        map.put("time", "0");

        return map;
    }
}
