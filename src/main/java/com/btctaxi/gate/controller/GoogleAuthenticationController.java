package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import eu.bitwalker.useragentutils.UserAgent;
import genesis.gate.error.ServiceError;
import genesis.gate.service.FA2Service;
import genesis.gate.service.GoogleAuthenticationService;
import genesis.gate.util.IP;
import genesis.gate.vendor.Totp;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/google")
public class GoogleAuthenticationController extends BaseController {
    private final String AUTH_BIND_GOOGLE_ACTION = "9";
    private final String AUTH_UNBIND_GOOGLE_ACTION = "10";

    private GoogleAuthenticationService googleAuthenticationService;
    private FA2Service fa2Service;

    public GoogleAuthenticationController(FA2Service fa2Service, GoogleAuthenticationService googleAuthenticationService) {
        this.fa2Service = fa2Service;
        this.googleAuthenticationService = googleAuthenticationService;
    }

    /**
     * 申请绑定google
     */
    @RequestMapping("/begin")
    public Map<String, Object> begin() {
        if (sess.getGoogleKey() != null)
            throw new ServiceError(ServiceError.AUTH_TYPE_GOOGLE_BINDED);
        sess.setNewGoogleKey(Totp.generateKey());
        Map<String, Object> map = new HashMap<>();
        map.put("google_key", sess.getNewGoogleKey());
        return map;
    }

    /**
     * 绑定google验证码
     */
    @RequestMapping("/bind")
    public Map<String, Object> bind(@RequestParam String code, HttpServletRequest request) {
        String phone = sess.getPhone();

        long now = System.currentTimeMillis() / 1000;
        String expect1 = Totp.generateCode(sess.getNewGoogleKey(), now);
        String expect2 = Totp.generateCode(sess.getNewGoogleKey(), now - 10);
        String expect3 = Totp.generateCode(sess.getNewGoogleKey(), now + 10);
        if (!expect1.equals(code) && !expect2.equals(code) && !expect3.equals(code))
            throw new ServiceError(ServiceError.AUTH_TYPE_GOOGLE_VERIFY_FAIL);

        JSONObject payload = new JSONObject();
        payload.put("code", code);
        payload.put("ip", IP.getIpAddr(request));

        UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        payload.put("device", ua.getOperatingSystem() + "，" + ua.getBrowser());

        Map<String, Object> fa2map = new HashMap<>();

        if (phone != null) {
            fa2map = fa2Service.getFA2Map(sess.getId(), AUTH_BIND_GOOGLE_ACTION, payload, null, phone);
        } else {
            googleAuthenticationService.bindAction(payload);
        }

        return fa2map;
    }

    /**
     * 解绑google验证码
     */
    @RequestMapping("/unbind")
    public Map<String, Object> unbind() {
        String googleKey = sess.getGoogleKey();
        String phone = sess.getPhone();

        JSONObject payload = new JSONObject();

        return fa2Service.getFA2Map(sess.getId(), AUTH_UNBIND_GOOGLE_ACTION, payload, googleKey, phone);
    }
}
