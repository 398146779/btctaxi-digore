package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RefundService extends BaseService {

    private final String REFUND_MINE_PROFITE_KEY_PREFIX = "refund_day_";

    private RefundService() {
    }

    /**
     * 获取某天总的挖矿量BTC
     */
    public DataMap getMineAndProfitTotal(String create_time) {
        // refund_profit_total字段应该和mine_amount_total含义一致，为快速解决问题用以下sql解决，这样前端不用修改
        String sql = "SELECT sum(mine_amount) as mine_amount_total, sum(mine_amount) as refund_profit_total, mine_unit, mine_unit as refund_profit_unit FROM tb_refund_commission WHERE create_time = ? group by mine_unit, refund_profit_unit";
        List<DataMap> pairs = data.query(sql, create_time);
        if (pairs.isEmpty()) {
            return new DataMap();
        }
        return pairs.get(0);
    }

    public Map<String, Object> getYesterdayMineAndProfitTotal() {
        Date yesterday = new Date(new Date().getTime() - 86400000L);
        String yesString = new SimpleDateFormat("yyyy-MM-dd").format(yesterday);

        // 先从缓存读取
        String key = REFUND_MINE_PROFITE_KEY_PREFIX + yesString;
        Map<String, Object> cacheData = kv.<String, Object>opsForHash().entries(key);
        if (cacheData != null && !cacheData.isEmpty()) {
            return cacheData;
        }

        Map<String, Object> res = new HashMap<>();

        // 从数据库读取
        DataMap dbData = this.getMineAndProfitTotal(yesString);

        res.put("mine_amount_total", Optional.ofNullable(dbData.getBig("mine_amount_total")).map(BigDecimal::toString).orElse("0"));
        res.put("refund_profit_total", Optional.ofNullable(dbData.getBig("refund_profit_total")).map(BigDecimal::toString).orElse("0"));
        res.put("mine_unit", Optional.ofNullable(dbData.getString("mine_unit")).orElse("TAXI"));
        res.put("refund_profit_unit", Optional.ofNullable(dbData.getString("refund_profit_unit")).orElse("TAXI"));
        res.put("yesterday", yesString);
        kv.opsForHash().putAll(key, res);
        kv.expire(key, 300L, TimeUnit.SECONDS);

        return res;
    }
}




