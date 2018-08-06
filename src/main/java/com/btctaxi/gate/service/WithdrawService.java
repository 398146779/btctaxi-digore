package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genesis.gate.error.ServiceError;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class WithdrawService extends BaseService {
    private final String BALANCE_WITHDRAW_ACTION = "11";

    private final String WITHDRAW_KEY_PREFIX = "WITHDRAW_";
    private final String WITHDRAW_RESEND_KEY_PREFIX = "WITHDRAW_RESEND_";

    private final long DAY1 = 86400000L;
    private final long MINUTE10 = 600000L;

    private AccountingService accountingService;
    private FA2Service fa2Service;

    public WithdrawService(AccountingService accountingService, FA2Service fa2Service) {
        this.accountingService = accountingService;
        this.fa2Service = fa2Service;
    }

    public Map<String, Object> create1(long userId, String chainName, String currencyName, String address, String memo, BigDecimal amount, String email, String nick, String phone, String googleKey, String host) {
        JSONObject payload = new JSONObject();
        payload.put("chainName", chainName);
        payload.put("currencyName", currencyName);
        payload.put("address", address);
        payload.put("memo", memo);
        payload.put("amount", amount);
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("nick", nick);
        payload.put("host", host);

        Map<String, Object> fa2map = new HashMap<>();

        if (googleKey != null || phone != null) {
            fa2map = fa2Service.getFA2Map(userId, BALANCE_WITHDRAW_ACTION, payload, googleKey, phone);
        } else {
            create2(payload);
        }

        return fa2map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public JSONObject create2(JSONObject payload) {
        long userId = payload.getLong("userId");
        String chainName = payload.getString("chainName");
        String currencyName = payload.getString("currencyName");
        String toAddress = payload.getString("address");
        String memo = payload.getString("memo");
        BigDecimal amount = payload.getBigDecimal("amount");
        String host = payload.getString("host");

        //查询有没有对应地址的转账记录
        boolean exists;
        if (memo != null) {
            JSONObject json = accountingService.post("/withdraw/latest", "user_id", userId, "chain_name", chainName, "currency_name", currencyName, "address", toAddress, "memo", memo);
            exists = json.containsKey("id");
        } else {
            JSONObject json = accountingService.post("/withdraw/latest", "user_id", userId, "chain_name", chainName, "currency_name", currencyName, "address", toAddress);
            exists = json.containsKey("id");
        }

        if (exists)
            create4(userId, chainName, currencyName, toAddress, memo, amount);
        else {
            Map<String, String> map = new HashMap<>();
            map.put("payload", payload.toJSONString());
            String auth = DigestUtils.md5Hex(UUID.randomUUID().toString() + userId);
            String key = WITHDRAW_KEY_PREFIX + auth;
            kv.<String, String>opsForHash().putAll(key, map);
            kv.expire(key, DAY1, TimeUnit.MILLISECONDS);

            String url = host + "/confirm/withdraw?auth=" + auth;
            String time = getTime();

            emailService.sendTemplate(payload.getString("email"), "withdraw-new", "zh_CN", amount.stripTrailingZeros().toPlainString(), currencyName, toAddress, time, url);

            JSONObject resendObj = new JSONObject();
            resendObj.put("email", payload.getString("email"));
            resendObj.put("name", "withdraw-new");
            resendObj.put("lang", "zh_CN");
            resendObj.put("amount", amount.stripTrailingZeros().toPlainString());
            resendObj.put("currencyName", currencyName);
            resendObj.put("toAddress", toAddress);
            resendObj.put("url", url);

            String resendKey = WITHDRAW_RESEND_KEY_PREFIX + userId;
            kv.opsForValue().set(resendKey, resendObj.toJSONString());
            kv.expire(resendKey, MINUTE10, TimeUnit.MILLISECONDS);

            JSONObject result = new JSONObject();
            result.put("email", true);
            return result;
        }

        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void create3(String auth) {
        String key = WITHDRAW_KEY_PREFIX + auth;
        Map<String, String> map = kv.<String, String>opsForHash().entries(key);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.WITHDRAW_CODE_NOT_EXISTS);

        kv.delete(key);

        JSONObject payload = JSON.parseObject(map.get("payload"));
        long userId = payload.getLong("userId");
        String chainName = payload.getString("chainName");
        String currencyName = payload.getString("currencyName");
        String memo = payload.getString("memo");
        String toAddress = payload.getString("address");
        BigDecimal amount = payload.getBigDecimal("amount");

        create4(userId, chainName, currencyName, toAddress, memo, amount);
    }

    public void create4(long userId, String chainName, String currencyName, String address, String memo, BigDecimal amount) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_id", String.valueOf(userId));
        params.add("chain_name", chainName);
        params.add("currency_name", currencyName);
        params.add("address", address);
        if (memo != null)
            params.add("memo", memo);
        params.add("amount", amount.toPlainString());
        accountingService.postMap("/withdraw/create", params);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void remove(long userId, long id) {
        accountingService.post("/withdraw/remove", "user_id", userId, "id", id);
    }

    @Transactional(readOnly = true)
    public void resend(long userId) {
        String val = kv.opsForValue().get(WITHDRAW_RESEND_KEY_PREFIX + userId);
        if (val == null)
            throw new ServiceError(ServiceError.WITHDRAW_CODE_NOT_EXISTS);

        JSONObject resendObj = JSON.parseObject(val);
        emailService.sendTemplate(resendObj.getString("email"), resendObj.getString("name"), resendObj.getString("lang"), resendObj.getString("amount"), resendObj.getString("currencyName"), resendObj.getString("toAddress"), getTime(), resendObj.getString("url"));
    }

    private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return f.format(new Date()) + "(UTC+8)";
    }
}
