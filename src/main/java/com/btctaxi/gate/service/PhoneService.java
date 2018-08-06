package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.controller.support.Session;
import genesis.gate.error.ServiceError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class PhoneService extends BaseService {
    private Session sess;
    private final long MINUTE5 = 300000L;
    private final long DAY1 = 86400000L;

    private final String PHONE_CHECK_KEY = "PHONE_CHECK_";
    private final String WITHDRAW_FORBIDDEN_PREFIX = "WITHDRAW_FORBIDDEN_";

    public PhoneService(Session sess) {
        this.sess = sess;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bind(long userId, int region_id, String phone) {
        String sql = "UPDATE tb_user SET region_id = ?, phone = ? WHERE id = ? AND phone IS NULL";
        int rowN = data.update(sql, region_id, phone, userId);
        if (rowN != 1)
            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unbind(long userId, String phone) {
        String sql = "UPDATE tb_user SET region_id = ?, phone = ? WHERE id = ? AND phone = ?";
        int rowN = data.update(sql, null, null, userId, phone);
        if (rowN != 1)
            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void bindAction(JSONObject payload) {
        String code = payload.getString("code");

        if (sess.getPhone() != null)
            throw new ServiceError(ServiceError.AUTH_TYPE_SMS_BINDED);
        if (sess.getNewPhone() == null)
            throw new ServiceError(ServiceError.AUTH_TYPE_SMS_REQUEST_ILLEGAL);
        Integer newRegionId = sess.getNewRegionId();
        String newPhone = sess.getNewPhone();
        String phoneCode = sess.getPhoneCode();
        sess.setNewRegionId(null);
        sess.setNewPhone(null);
        sess.setPhoneCode(null);

        long now = System.currentTimeMillis();
        if (now - sess.getPhoneCodeTime() > MINUTE5)
            throw new ServiceError(ServiceError.AUTH_CODE_EXPIRED);
        if (!phoneCode.equals(code))
            throw new ServiceError(ServiceError.AUTH_TYPE_SMS_VERIFY_FAIL);

        bind(sess.getId(), newRegionId, newPhone);

        sess.setRegionId(newRegionId);
        sess.setPhone(newPhone);
    }

    public void unbindAction() {
        if (sess.getPhone() == null)
            throw new ServiceError(ServiceError.AUTH_TYPE_SMS_UNBINDED);

        unbind(sess.getId(), sess.getPhone());

        String key = WITHDRAW_FORBIDDEN_PREFIX + sess.getId();
        kv.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
        kv.expire(key, DAY1, TimeUnit.MILLISECONDS);

        sess.setRegionId(null);
        sess.setPhone(null);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void sendCode(Integer region_id, String phone, String code) {
        String sql = "SELECT phone FROM tb_user WHERE phone = ?";
        DataMap user = data.queryOne(sql, phone);

        if (user != null)
            throw new ServiceError(ServiceError.FA2_PHONE_EXISTS);

        String key = PHONE_CHECK_KEY + region_id + phone;
        String val = kv.opsForValue().get(key);
        if (val != null)
            throw new ServiceError(ServiceError.AUTH_PHONE_CODE_LIMIT);

        kv.opsForValue().set(key, "1", 60000L, TimeUnit.MILLISECONDS);

        sql = "SELECT operator, operator_backup FROM tb_region WHERE id = ?";
        DataMap region = data.queryOne(sql, region_id);

        int operator = region.getInt("operator");
        int operator_backup = region.getInt("operator_backup");

        //operator = operator == 1 ? operator_backup : operator;

        if (region_id != 86 && operator == 3)
            operator = operator_backup;

        sms.sendCode(operator, region_id, phone, code, distConfig.getSmsSign());
    }

    @Transactional(readOnly = true)
    public void isExists(String phone) {
        String sql = "SELECT id FROM tb_user WHERE phone = ?";
        DataMap user = data.queryOne(sql, phone);
        if (user != null)
            throw new ServiceError(ServiceError.SIGNUP_PHONE_EXISTS);
    }
}
