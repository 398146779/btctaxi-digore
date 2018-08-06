package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.controller.support.Session;
import genesis.gate.error.ServiceError;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordService extends BaseService {

    private final String PASSWORD_MODIFY_KEY_PREFIX = "password_modify_";
    private final String PASSWORD_RESET_KEY_PREFIX = "password_reset_";
    private final String PASSWORD_RESET_AUTH_KEY_PREFIX = "password_reset_auth_";
    private final String WITHDRAW_FORBIDDEN_PREFIX = "WITHDRAW_FORBIDDEN_";
    private final String PASSWORD_MODIFY_ACTION = "2";
    private final String PASSWORD_RESET_ACTION = "3";

    private final long MINUTE1 = 60000L;
    private final long MINUTE10 = 600000L;
    private final long MINUTE30 = 1800000L;
    private final long DAY1 = 86400000L;

    private FA2Service fa2Service;
    private Session sess;

    public PasswordService(FA2Service fa2Service, Session sess) {
        this.fa2Service = fa2Service;
        this.sess = sess;
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> modify(long userId, String currentPassword, String newPassword) {
        String key = PASSWORD_MODIFY_KEY_PREFIX + userId;
        Map<String, String> kvmap = kv.<String, String>opsForHash().entries(key);

        Map<String, Object> fa2map = new HashMap<>();

        String retryStr = kvmap.get("retry");
        int retry = retryStr == null ? 0 : Integer.parseInt(retryStr);

        if (retry >= 10)
            throw new ServiceError(ServiceError.PASSWORD_MODIFY_UNAUTH_LIMIT);

        retry++;
        kvmap.put("retry", String.valueOf(retry));
        kv.opsForHash().putAll(key, kvmap);
        if (retryStr == null)
            kv.expire(key, 86400, TimeUnit.SECONDS);

        String sql = "SELECT google_key, phone, salt, pass FROM tb_user WHERE id = ?";
        DataMap user = data.queryOne(sql, userId);
        String googleKey = user.getString("google_key");
        String phone = user.getString("phone");
        String salt = user.getString("salt");
        String pass = user.getString("pass");

        if (!pass.equals(DigestUtils.sha256Hex(currentPassword + salt)))
            throw new ServiceError(ServiceError.PASSWORD_MODIFY_UNAUTH, retry);

        JSONObject payload = new JSONObject();
        payload.put("current_password", currentPassword);
        payload.put("new_password", newPassword);

        if (googleKey != null || phone != null) {
            fa2map = fa2Service.getFA2Map(userId, PASSWORD_MODIFY_ACTION, payload, googleKey, phone);
        } else {
            modifyAction(payload);
        }

        return fa2map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void modifyAction(JSONObject payload) {
        String currentPassword = payload.getString("current_password");
        String newPassword = payload.getString("new_password");
        long userId = sess.getId();

        String key = PASSWORD_MODIFY_KEY_PREFIX + userId;
        Map<String, String> map = kv.<String, String>opsForHash().entries(key);

        String sql = "SELECT salt FROM tb_user WHERE id = ?";
        DataMap user = data.queryOne(sql, userId);
        String salt = user.getString("salt");

        sql = "UPDATE tb_user SET pass = ? WHERE id = ? AND pass = ?";
        int success = data.update(sql, DigestUtils.sha256Hex(newPassword + salt), userId, DigestUtils.sha256Hex(currentPassword + salt));

        if (success == 1) {
            kv.delete(key);
        } else {
            throw new ServiceError(ServiceError.PASSWORD_MODIFY_UNAUTH, map.get("retry"));
        }

        String forbiddenKey = WITHDRAW_FORBIDDEN_PREFIX + userId;
        kv.opsForValue().set(forbiddenKey, String.valueOf(System.currentTimeMillis()));
        kv.expire(forbiddenKey, DAY1, TimeUnit.MILLISECONDS);
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> forgot(String email, String host) {
        String sql = "SELECT id, google_key, phone FROM tb_user WHERE email = ?";
        DataMap user = data.queryOne(sql, email);
        if (user == null)
            throw new ServiceError(ServiceError.FOGGOT_EMAIL_NOT_EXISTS);

        long userId = user.getLong("id");
        String googleKey = user.getString("google_key");
        String phone = user.getString("phone");

        String key = PASSWORD_RESET_KEY_PREFIX + userId;
        Map<String, String> kvmap = kv.<String, String>opsForHash().entries(key);

        String emailTimeStr = kvmap.get("email_time");
        long emailTime = emailTimeStr == null ? 0L : Long.parseLong(emailTimeStr);
        long now = System.currentTimeMillis();
        if (now - emailTime < MINUTE1)
            throw new ServiceError(ServiceError.FORGOT_EMAIL_TIME_LIMIT);

        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        Map<String, Object> fa2map = new HashMap<>();

        if (googleKey != null || phone != null) {
            fa2map = fa2Service.getFA2Map(userId, PASSWORD_RESET_ACTION, payload, googleKey, phone);
        } else {
            forgotAction(payload, host);
        }

        kvmap.put("email_time", String.valueOf(now));
        kv.opsForHash().putAll(key, kvmap);
        kv.expire(key, MINUTE1, TimeUnit.SECONDS);

        return fa2map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void forgotAction(JSONObject payload, String host) {
        long userId = payload.getLong("userId");
        String sql = "SELECT id, email, google_key, phone, nick, locale FROM tb_user WHERE id = ?";
        DataMap user = data.queryOne(sql, userId);

        String email = user.getString("email");

        String auth = DigestUtils.md5Hex(UUID.randomUUID().toString() + userId);
        String key = PASSWORD_RESET_AUTH_KEY_PREFIX + auth;
        Map<String, String> kvmap = kv.<String, String>opsForHash().entries(key);
        kvmap.put("userId", String.valueOf(userId));
        kvmap.put("email", email);

        kv.<String, String>opsForHash().putAll(key, kvmap);
        kv.expire(key, MINUTE30, TimeUnit.MILLISECONDS);

        String url = host + "/account/reset?auth=" + auth;
        emailService.sendTemplate(email, "reset", "zh_CN", getTime(), url);

        long now = System.currentTimeMillis();
        sess.setActivateCode(auth);
        sess.setActivateSendTime(now);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void reset(String auth, String password) {
        String key = PASSWORD_RESET_AUTH_KEY_PREFIX + auth;
        Map<String, String> kvmap = kv.<String, String>opsForHash().entries(key);

        if (kvmap.isEmpty())
            throw new ServiceError(ServiceError.FORGOT_AUTH_KEY_NOT_EXISTS);

        long userId = Long.parseLong(kvmap.get("userId"));
        kv.delete(key);

        String salt = UUID.randomUUID().toString();
        String sha = DigestUtils.sha256Hex(password + salt);
        String sql = "UPDATE tb_user SET pass = ?, salt = ? WHERE id = ?";
        data.update(sql, sha, salt, userId);

        String forbiddenKey = WITHDRAW_FORBIDDEN_PREFIX + userId;
        kv.opsForValue().set(forbiddenKey, String.valueOf(System.currentTimeMillis()));
        kv.expire(forbiddenKey, DAY1, TimeUnit.MILLISECONDS);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void reactivate(String auth, String host) {
        String key = PASSWORD_RESET_AUTH_KEY_PREFIX + auth;
        Map<String, String> map = kv.<String, String>opsForHash().entries(key);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.ACTIVATE_CODE_NOT_EXISTS);

        String email = map.get("email");

        String url = host + "/account/reset?auth=" + auth;
        emailService.sendTemplate(email, "reset", "zh_CN", getTime(), url);
    }

    private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return f.format(new Date(new Date().getTime() + 1800000)) + "(UTC+8)";
    }
}
