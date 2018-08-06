package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.controller.support.Session;
import genesis.gate.error.ConcurrentError;
import genesis.gate.error.ServiceError;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ApiService extends BaseService {
    private final String API_PREFIX = "API_";
    private final String API_CREATE_ACTION = "4";
    private final String API_UPDATE_ACTION = "5";
    private final String API_REMOVE_ACTION = "6";

    private final String API_CREATE_CONFIRM_PREFIX = "API_CONFIRM_";
    private final long HOUR2 = 7200000L;

    private FA2Service fa2Service;
    private Session sess;

    public ApiService(FA2Service fa2Service, Session sess) {
        this.fa2Service = fa2Service;
        this.sess = sess;
    }

    @Transactional(readOnly = true)
    public List<DataMap> permissions() {
        String sql = "SELECT id, name FROM tb_api_permission";
        return data.query(sql);
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> create(long permission, String label, String host) {
        String googleKey = sess.getGoogleKey();
        String phone = sess.getPhone();

        if (googleKey == null && phone == null)
            throw new ServiceError(ServiceError.FA2_BIND_NEEDED);

        JSONObject payload = new JSONObject();
        payload.put("permission", permission);
        payload.put("label", HtmlUtils.htmlEscape(label));
        payload.put("userId", sess.getId());

        Map<String, Object> fa2map = fa2Service.getFA2Map(sess.getId(), API_CREATE_ACTION, payload, googleKey, phone);

        return fa2map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createAction(JSONObject payload, String host) {
        Map<String, String> map = new HashMap<>();
        map.put("payload", payload.toJSONString());
        String key = DigestUtils.md5Hex(UUID.randomUUID().toString() + sess.getId());
        String apiKey = API_CREATE_CONFIRM_PREFIX + key;
        kv.<String, String>opsForHash().putAll(apiKey, map);
        kv.expire(apiKey, HOUR2, TimeUnit.MILLISECONDS);

        String url = host + "/user/profile?task=api&auth=" + key;
        String label = payload.getString("label");

        emailService.sendTemplate(sess.getEmail(), "api-create", "zh_CN", label, url);
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataMap create_confirm(String auth) {
        String key = API_CREATE_CONFIRM_PREFIX + auth;
        Map<String, String> kvmap = kv.<String, String>opsForHash().entries(key);
        if (kvmap == null || kvmap.isEmpty())
            throw new ServiceError(ServiceError.API_AUTH_NOT_EXISTS);

        JSONObject payload = JSON.parseObject(kvmap.get("payload"));
        long userId = sess.getId();
        if (userId != payload.getLong("userId"))
            throw new ServiceError(ServiceError.API_AUTH_ILLEGAL);

        long permission = payload.getLongValue("permission");
        String label = payload.getString("label");

        SecureRandom rand = new SecureRandom();
        byte[] keyBs = new byte[16];
        rand.nextBytes(keyBs);
        byte[] secretBs = new byte[32];
        rand.nextBytes(secretBs);
        String apiKey = Hex.encodeHexString(keyBs);
        rand.nextBytes(secretBs);
        String apiSecret = Hex.encodeHexString(secretBs);

        String sql = "INSERT INTO tb_api(user_id, api_key, api_secret, permission, label) VALUES(?, ?, ?, ?, ?)";
        long id = data.insert(sql, userId, apiKey, apiSecret, permission, label);

        Map<String, String> map = new HashMap<>();
        map.put("user_id", String.valueOf(userId));
        map.put("api_secret", apiSecret);
        map.put("permission", String.valueOf(permission));
        String mapKey = API_PREFIX + apiKey;
        kv.opsForHash().putAll(mapKey, map);

        kv.delete(key);

        DataMap result = new DataMap();
        result.put("id", id);
        result.put("api_key", apiKey);
        result.put("api_secret", apiSecret);
        return result;
    }

    @Transactional(readOnly = true)
    public List<DataMap> list(long userId) {
        String sql = "SELECT id, api_key, api_secret, permission, label FROM tb_api WHERE user_id = ?";
        List<DataMap> apis = data.query(sql, userId);
        apis.forEach(api ->
        {
            String api_key = api.getString("api_key");
            api.put("api_key", api_key.substring(0, 7) + "********");

            String api_secret = api.getString("api_secret");
            api.put("api_secret", api_secret.substring(0, 7) + "************************");
        });
        return apis;
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> update(long apiId, long permission) {
        String googleKey = sess.getGoogleKey();
        String phone = sess.getPhone();

        JSONObject payload = new JSONObject();
        payload.put("api_id", apiId);
        payload.put("permission", permission);

        Map<String, Object> fa2map = new HashMap<>();

        if (googleKey != null || phone != null) {
            fa2map = fa2Service.getFA2Map(sess.getId(), API_UPDATE_ACTION, payload, googleKey, phone);
        } else {
            updateAction(payload);
        }

        return fa2map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataMap updateAction(JSONObject payload) {
        long userId = sess.getId();
        long apiId = payload.getLong("api_id");
        String permission = payload.getString("permission");

        SecureRandom rand = new SecureRandom();
        byte[] bs = new byte[32];
        rand.nextBytes(bs);
        String apiSecret = Hex.encodeHexString(bs);

        String sql = "SELECT api_key FROM tb_api WHERE id = ? AND user_id = ?";
        DataMap api = data.queryOne(sql, apiId, userId);
        String apiKey = api.getString("api_key");

        sql = "UPDATE tb_api SET api_secret = ?, permission = ? WHERE id = ? AND user_id = ?";
        int rowN = data.update(sql, apiSecret, permission, apiId, userId);
        if (rowN != 1)
            throw new ConcurrentError();

        Map<String, String> map = new HashMap<>();
        map.put("api_secret", apiSecret);
        map.put("permission", String.valueOf(permission));
        String mapKey = API_PREFIX + apiKey;
        kv.opsForHash().putAll(mapKey, map);

        DataMap result = new DataMap();
        result.put("api_key", apiKey);
        result.put("api_secret", apiSecret);
        return result;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void remove(long apiId, long userId) {
        String sql = "SELECT api_key FROM tb_api WHERE id = ? AND user_id = ?";
        DataMap api = data.queryOne(sql, apiId, userId);
        String apiKey = api.getString("api_key");

        sql = "DELETE FROM tb_api WHERE id = ? AND user_id = ?";
        int rowN = data.update(sql, apiId, userId);
        if (rowN != 1)
            throw new ConcurrentError();

        String mapKey = API_PREFIX + apiKey;
        kv.delete(mapKey);
    }
}
