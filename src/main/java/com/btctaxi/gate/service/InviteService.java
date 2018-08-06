package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InviteService extends BaseService {
    private final String INVITE_RANKING_KEY_PREFIX = "MARKETING_INVITE_RANKING";
    private final String MARKETING_INVITE_KEY_PREFIX = "MARKETING_INVITE_USER_";

    public InviteService() {
    }

    @Transactional(readOnly = true)
    public List<DataMap> ranking() {
        List<DataMap> ranks = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<String>> lists = kv.opsForZSet().reverseRangeWithScores(INVITE_RANKING_KEY_PREFIX, 0, 9);
        lists.forEach(user ->
        {
            String name = user.getValue().replaceAll("(\\d{3}).*(\\d{4})", "$1****$2");
            long amount = Math.round(user.getScore());
            DataMap map = new DataMap();
            map.put("user", name);
            map.put("amount", amount);
            ranks.add(map);
        });
        return ranks;
    }

    @Transactional(readOnly = true)
    public Map<String, String> invitees(long userId) {
        String key = MARKETING_INVITE_KEY_PREFIX + userId;
        return kv.<String, String>opsForHash().entries(key);
    }
}
