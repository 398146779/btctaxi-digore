package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.btctaxi.common.DataMap;
import com.btctaxi.gate.config.AccountingConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AccountingService extends BaseService {
    private RestTemplate http;
    private AccountingConfig accountingConfig;

    private final String TOTAL_AVAILABLE_PREFIX = "total_available_";

    private AccountingService(RestTemplate http, AccountingConfig accountingConfig) {
        this.http = http;
        this.accountingConfig = accountingConfig;
    }

    public <T> T post(String uri, Object... pairs) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            map.add((String) pairs[i], pairs[i + 1] instanceof BigDecimal ? ((BigDecimal) pairs[i + 1]).toPlainString() : pairs[i + 1].toString());
        return (T) postMap(uri, map);
    }

    public Object postMap(String uri, MultiValueMap<String, String> map) {
        ResponseEntity<String> res = http.postForEntity(accountingConfig.getHost() + uri, map, String.class);
        String body = res.getBody();
        Object json = JSON.parse(body);
        return json;
    }

    /**
     * 获取某个币的所有持仓量
     */
    public BigDecimal getTotalAvailable(String currencyName) {
        // 先从缓存读取
        String key = TOTAL_AVAILABLE_PREFIX + currencyName;
        String cacheData = kv.opsForValue().get(key);
        if (cacheData != null) {
            return new BigDecimal(cacheData);
        }

        // 从数据库读取
        String sql = "SELECT sum(available) as total_available, sum(ordering) as total_ordering, sum(withdrawing) as total_withdrawing, sum(locking) as total_locking FROM accounting_balance WHERE currency_name = ?";
        List<DataMap> pairs = data.query(sql, currencyName);

        BigDecimal total_available = Optional.ofNullable(pairs.get(0).getBig("total_available")).orElse(BigDecimal.ZERO);
        BigDecimal total_ordering = Optional.ofNullable(pairs.get(0).getBig("total_ordering")).orElse(BigDecimal.ZERO);
        BigDecimal total_withdrawing = Optional.ofNullable(pairs.get(0).getBig("total_withdrawing")).orElse(BigDecimal.ZERO);
        BigDecimal total_locking = Optional.ofNullable(pairs.get(0).getBig("total_locking")).orElse(BigDecimal.ZERO);

        BigDecimal total = total_available.add(total_ordering).add(total_withdrawing).add(total_locking);

        kv.opsForValue().set(key, total.toString());
        kv.expire(key, 600L, TimeUnit.SECONDS);

        return total;
    }
}




