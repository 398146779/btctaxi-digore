package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONObject;
import com.btctaxi.gate.controller.support.Session;
import com.btctaxi.gate.error.ServiceError;
import com.btctaxi.gate.util.SMS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleAuthenticationService extends BaseService {
    private SMS sms;
    private Session sess;
    private final long SECOND50 = 50000L;
    private final long DAY1 = 86400000L;
    private final String OTP_PREFIX = "OTP_";
    private final String WITHDRAW_FORBIDDEN_PREFIX = "WITHDRAW_FORBIDDEN_";

    public GoogleAuthenticationService(SMS sms, Session sess) {
        this.sms = sms;
        this.sess = sess;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bind(long userId, String googleKey) {
        String sql = "UPDATE tb_user SET google_key = ? WHERE id = ? AND google_key IS NULL";
        int rowN = data.update(sql, googleKey, userId);
        if (rowN != 1)
            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);
    }

    /**
     * 绑定google验证码
     */
    @Transactional(rollbackFor = Throwable.class)
    public void bindAction(JSONObject payload) {
        if (sess.getGoogleKey() != null)
            throw new ServiceError(ServiceError.AUTH_TYPE_GOOGLE_BINDED);
        if (sess.getNewGoogleKey() == null)
            throw new ServiceError(ServiceError.AUTH_TYPE_GOOGLE_NO_REQUESTED);

        bind(sess.getId(), sess.getNewGoogleKey());

        sess.setGoogleKey(sess.getNewGoogleKey());
        sess.setNewGoogleKey(null);

        String otpKey = OTP_PREFIX + sess.getId();
        kv.opsForValue().set(otpKey, payload.getString("code"), SECOND50, TimeUnit.MILLISECONDS);

        String device = payload.getString("device");
        String ip = payload.getString("ip");

        emailService.sendTemplate(sess.getEmail(), "bind-google", "zh_CN", device, getTime(), ip);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unbind(long userId, String googleKey) {
        String sql = "UPDATE tb_user SET google_key = ? WHERE id = ? AND google_key = ?";
        int rowN = data.update(sql, null, userId, googleKey);
        if (rowN != 1)
            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);
    }

    /**
     * 解绑google验证码
     */
    public void unbindAction() {
        if (sess.getGoogleKey() == null)
            throw new ServiceError(ServiceError.AUTH_TYPE_GOOGLE_UNBINDED);

        unbind(sess.getId(), sess.getGoogleKey());

        String key = WITHDRAW_FORBIDDEN_PREFIX + sess.getId();
        kv.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
        kv.expire(key, DAY1, TimeUnit.MILLISECONDS);

        sess.setGoogleKey(null);
    }

    private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return f.format(new Date()) + "(UTC+8)";
    }
}
