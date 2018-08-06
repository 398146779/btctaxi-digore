package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.error.ServiceError;
import genesis.gate.vendor.Totp;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 二步验证统一处理服务
 */
@Service
public class FA2Service extends BaseService implements ApplicationContextAware {
    private ApplicationContext ctx;

    private final String FA2_PREFIX = "FA2_";
    private final String OTP_PREFIX = "OTP_";

    private final long SECOND30 = 30000L;
    private final long SECOND50 = 50000L;
    private final long MINUTE5 = 300000L;
    private final int MAX_RETRY = 3;

    private final String SIGN_SIGNIN_ACTION = "1";
    private final String PASSWORD_MODIFY_ACTION = "2";
    private final String PASSWORD_RESET_ACTION = "3";
    private final String API_CREATE_ACTION = "4";
    private final String API_UPDATE_ACTION = "5";
    private final String AUTH_BIND_PHONE_ACTION = "7";
    private final String AUTH_UNBIND_PHONE_ACTION = "8";
    private final String AUTH_BIND_GOOGLE_ACTION = "9";
    private final String AUTH_UNBIND_GOOGLE_ACTION = "10";
    private final String BALANCE_WITHDRAW_ACTION = "11";

    @Transactional(rollbackFor = Throwable.class)
    public String begin(long userId, Map<String, String> map) {
        String key = DigestUtils.md5Hex(UUID.randomUUID().toString() + userId);

        String sql = "SELECT id, google_key, region_id, phone FROM tb_user WHERE id = ?";
        DataMap user = data.queryOne(sql, userId);

        String googleKey = user.getString("google_key");
        Integer region_id = user.getInt("region_id");
        String phone = user.getString("phone");

        map.put("user_id", String.valueOf(userId));
        if (googleKey != null)
            map.put("google_key", googleKey);
        if (phone != null) {
            map.put("region_id", String.valueOf(region_id));
            map.put("phone", phone);
        }

        String fa2Key = FA2_PREFIX + key;
        kv.<String, String>opsForHash().putAll(fa2Key, map);
        kv.expire(fa2Key, MINUTE5, TimeUnit.MILLISECONDS);

        return key;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void sms(String key) {
        String fa2Key = FA2_PREFIX + key;
        Map<String, String> map = kv.<String, String>opsForHash().entries(fa2Key);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.FA2_KEY_NOT_EXISTS);

        // 取发短信信息
        Integer region_id = Integer.parseInt(map.get("region_id"));
        String phone = map.get("phone");
        String phoneTimeStr = map.get("phone_time");
        long phoneTime = phoneTimeStr == null ? 0L : Long.parseLong(phoneTimeStr);
        long now = System.currentTimeMillis();
        if (now - phoneTime < SECOND30)
            throw new ServiceError(ServiceError.AUTH_PHONE_CODE_LIMIT);

        // 更新发短信时间
        String code = sms.generateCode();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("phone_code", code);
        attrs.put("phone_time", String.valueOf(now));
        kv.<String, String>opsForHash().putAll(fa2Key, attrs);

        String sql = "SELECT operator, operator_backup FROM tb_region WHERE id = ?";
        DataMap region = data.queryOne(sql, region_id);

        int operator = region.getInt("operator");
        int operator_backup = region.getInt("operator_backup");

        //operator = operator == 1 ? operator_backup : operator;

        if (region_id != 86 && operator == 3)
            operator = operator_backup;

        sms.sendCode(operator, region_id, phone, code, distConfig.getSmsSign());
    }

    @Transactional(rollbackFor = Throwable.class)
    public Object verify(String type, String key, String code, String host, int kyc, long userId) {
        Object result = null;
        String fa2Key = FA2_PREFIX + key;
        Map<String, String> map = kv.<String, String>opsForHash().entries(fa2Key);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.FA2_KEY_NOT_EXISTS);

        String uid = map.get("user_id");
        if (userId > 0 && Long.parseLong(uid) != userId)
            throw new ServiceError(ServiceError.FA2_KEY_NOT_EXISTS);

        if ("google".equals(type)) {
            String googleKey = map.get("google_key");
            String retryStr = map.get("retry");
            int retry = retryStr == null ? 0 : Integer.parseInt(retryStr);

            long now = System.currentTimeMillis() / 1000;
            String expect1 = Totp.generateCode(googleKey, now);
            String expect2 = Totp.generateCode(googleKey, now - 10);
            String expect3 = Totp.generateCode(googleKey, now + 10);
            if (!expect1.equals(code) && !expect2.equals(code) && !expect3.equals(code)) {
                retry++;
                if (retry >= MAX_RETRY) {
                    kv.delete(fa2Key);
                    throw new ServiceError(ServiceError.FA2_GOOGLE_AUTH_MAX_RETRY);
                }
                kv.opsForHash().put(fa2Key, "retry", String.valueOf(retry));
                throw new ServiceError(ServiceError.FA2_GOOGLE_AUTH_FAIL);
            }

            String otpKey = OTP_PREFIX + uid;
            String prevCode = kv.opsForValue().get(otpKey);
            if (prevCode != null && prevCode.equals(code))
                throw new ServiceError(ServiceError.FA2_GOOGLE_AUTH_DUPLICTE);
            kv.opsForValue().set(otpKey, code, SECOND50, TimeUnit.MILLISECONDS);
        } else {
            String phoneTimeStr = map.get("phone_time");
            long phoneTime = phoneTimeStr == null ? 0L : Long.parseLong(phoneTimeStr);
            long now = System.currentTimeMillis();

            if (now - phoneTime > MINUTE5)
                throw new ServiceError(ServiceError.FA2_PHONE_TIME_LIMIT);
            String phoneCode = map.get("phone_code");
            if (!phoneCode.equals(code)) {
                throw new ServiceError(ServiceError.FA2_PHONE_AUTH_FAIL);
            }
        }

        // 验证成功直接删除
        kv.delete(fa2Key);

        String action = map.get("action");
        JSONObject payload = JSON.parseObject(map.get("payload"));
        switch (action) {
            case PASSWORD_MODIFY_ACTION:
                PasswordService modifyPasswordService = ctx.getBean(PasswordService.class);
                modifyPasswordService.modifyAction(payload);
                break;
            case SIGN_SIGNIN_ACTION:
                SignService signService = ctx.getBean(SignService.class);
                result = signService.signinAction(payload, null);
                break;
            case PASSWORD_RESET_ACTION:
                PasswordService resetPasswordService = ctx.getBean(PasswordService.class);
                resetPasswordService.forgotAction(payload, host);
                break;
            case API_CREATE_ACTION:
                ApiService createApiService = ctx.getBean(ApiService.class);
                createApiService.createAction(payload, host);
                break;
            case API_UPDATE_ACTION:
                ApiService updateApiService = ctx.getBean(ApiService.class);
                updateApiService.updateAction(payload);
                break;
            case AUTH_BIND_PHONE_ACTION:
                PhoneService bindPhoneService = ctx.getBean(PhoneService.class);
                bindPhoneService.bindAction(payload);
                break;
            case AUTH_UNBIND_PHONE_ACTION:
                PhoneService unbindPhoneService = ctx.getBean(PhoneService.class);
                unbindPhoneService.unbindAction();
                break;
            case AUTH_BIND_GOOGLE_ACTION:
                GoogleAuthenticationService bindGoogleAuthenticationService = ctx.getBean(GoogleAuthenticationService.class);
                bindGoogleAuthenticationService.bindAction(payload);
                break;
            case AUTH_UNBIND_GOOGLE_ACTION:
                GoogleAuthenticationService unbindGoogleAuthenticationService = ctx.getBean(GoogleAuthenticationService.class);
                unbindGoogleAuthenticationService.unbindAction();
                break;
            case BALANCE_WITHDRAW_ACTION:
                WithdrawService withdrawService = ctx.getBean(WithdrawService.class);
                result = withdrawService.create2(payload);
            default:
                break;
        }

        if (result == null)
            result = new JSONObject();

        return result;
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> getFA2Map(long userId, String action, JSONObject payload, String googleKey, String phone) {
        Map<String, Object> fa2map = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("action", action);
        map.put("payload", payload.toJSONString());

        String fa2key = begin(userId, map);
        fa2map.put("google", googleKey != null);
        fa2map.put("phone", phone != null);
        fa2map.put("key", fa2key);

        return fa2map;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }
}
