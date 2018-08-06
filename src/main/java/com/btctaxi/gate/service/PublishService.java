package com.btctaxi.gate.service;

import genesis.gate.config.EngineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisShardInfo;

@Service
public class PublishService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private StringRedisTemplate kv;

    public PublishService(EngineConfig engineConfig) {
        JedisShardInfo cfg = new JedisShardInfo(engineConfig.getMessageHost());
        JedisConnectionFactory f = new JedisConnectionFactory(cfg);
        kv = new StringRedisTemplate(f);
    }

    @Retryable(value = Throwable.class, backoff = @Backoff(delay = 100L, multiplier = 2))
    public void publish(String channel, String message) {
        Long n = kv.execute((RedisCallback<Long>) (conn) -> conn.publish(channel.getBytes(), message.getBytes()));
        if (n < 1) {
            log.error("redis publish error: {}", message);
            throw new RuntimeException("redis publish error");
        }
    }
}
