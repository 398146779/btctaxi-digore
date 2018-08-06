package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Service;

@Service
public class NotificationService extends BaseService {

    private final String NOTIFICATION_KEY_PREFIX = "NOTIFICATION_";

    public JSONArray take(long userId) {
        JSONArray results = new JSONArray();

        String notificaiton;
        while ((notificaiton = kv.opsForList().rightPop(NOTIFICATION_KEY_PREFIX + userId)) != null) {
            results.add(JSON.parseObject(notificaiton));
        }

        return results;
    }
}