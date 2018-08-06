package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import genesis.gate.config.GeetestConfig;
import genesis.gate.error.BadRequestError;
import genesis.gate.error.ServiceError;
import genesis.gate.service.FA2Service;
import genesis.gate.service.PhoneService;
import genesis.gate.util.SMS;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/phone")
public class PhoneController extends BaseController {
    private final long MINUTE1 = 60000L;
    private final long SECOND60 = 60000L;

    private final String AUTH_BIND_PHONE_ACTION = "7";
    private final String AUTH_UNBIND_PHONE_ACTION = "8";

    private PhoneService phoneService;
    private FA2Service fa2Service;
    private GeetestConfig geetestConfig;
    private SMS sms;

    public PhoneController(PhoneService phoneService, FA2Service fa2Service, GeetestConfig geetestConfig, SMS sms) {
        this.phoneService = phoneService;
        this.fa2Service = fa2Service;
        this.geetestConfig = geetestConfig;
        this.sms = sms;
    }

    /**
     * 绑定手机前发送验证码
     */
    @RequestMapping("/begin")
    public void begin(@RequestParam Integer region_id, @RequestParam String phone) {
        if (region_id == null || phone == null)
            throw new BadRequestError();

        if (sess.getPhone() != null)
            throw new ServiceError(ServiceError.AUTH_TYPE_SMS_BINDED);
        long now = System.currentTimeMillis();
        if (now - sess.getPhoneCodeTime() < SECOND60)
            throw new ServiceError(ServiceError.AUTH_PHONE_CODE_LIMIT);

        String code = sms.generateCode();
        sess.setNewRegionId(region_id);
        sess.setNewPhone(phone);
        sess.setPhoneCode(code);
        sess.setPhoneCodeTime(now);

        phoneService.sendCode(region_id, phone, code);
    }

    /**
     * 手机绑定
     */
    @RequestMapping("/bind")
    public Map<String, Object> bind(@RequestParam String code) {
        String googleKey = sess.getGoogleKey();

        JSONObject payload = new JSONObject();
        payload.put("code", code);

        Map<String, Object> fa2map = new HashMap<>();

        if (googleKey != null) {
            fa2map = fa2Service.getFA2Map(sess.getId(), AUTH_BIND_PHONE_ACTION, payload, googleKey, null);
        } else {
            phoneService.bindAction(payload);
        }

        return fa2map;
    }

    /**
     * 手机解绑
     */
    @RequestMapping("/unbind")
    public Map<String, Object> unbind() {
        String googleKey = sess.getGoogleKey();
        String phone = sess.getPhone();

        JSONObject payload = new JSONObject();

        return fa2Service.getFA2Map(sess.getId(), AUTH_UNBIND_PHONE_ACTION, payload, googleKey, phone);
    }
}
