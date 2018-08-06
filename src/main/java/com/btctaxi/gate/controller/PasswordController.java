package com.btctaxi.gate.controller;

import genesis.gate.config.GeetestConfig;
import genesis.gate.error.ServiceError;
import genesis.gate.service.PasswordService;
import genesis.gate.vendor.GeetestLib;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/password")
public class PasswordController extends BaseController {
    private PasswordService passwordService;
    private GeetestConfig geetestConfig;

    private final int MINUTE1 = 60000;

    public PasswordController(PasswordService passwordService, GeetestConfig geetestConfig) {
        this.passwordService = passwordService;
        this.geetestConfig = geetestConfig;
    }

    @RequestMapping("/modify")
    public Map<String, Object> modify(@RequestParam String current_password, @RequestParam String new_password) {
        return passwordService.modify(sess.getId(), current_password, new_password);
    }

    @RequestMapping("/forgot")
    @Any
    public Map<String, Object> forgot(@RequestParam String email, @RequestParam String geetest_challenge, @RequestParam String geetest_validate, @RequestParam String geetest_seccode, HttpServletRequest req) {
        GeetestLib gtSdk = new GeetestLib(geetestConfig.getId(), geetestConfig.getKey(), geetestConfig.isNewFailBack());

        HashMap<String, String> geetestMap = new HashMap<>();
        geetestMap.put("ip_address", req.getRemoteAddr());

        int gt_server_status_code = gtSdk.preProcess(geetestMap);
        if(gt_server_status_code != 1)
            throw new ServiceError(ServiceError.GEETEST_AUTH_FAIL);

        int gtResult = gtSdk.geetestSecondCheck(gt_server_status_code, geetest_challenge, geetest_validate, geetest_seccode, req.getRemoteAddr());
        if (gtResult != 1)
            throw new ServiceError(ServiceError.GEETEST_AUTH_FAIL);

        return passwordService.forgot(email, "https://" + req.getServerName());
    }

    @RequestMapping("reset")
    @Any
    public void reset(@RequestParam String auth, @RequestParam String password) {
        passwordService.reset(auth, password);
    }

    /**
     * 重新发送激活码
     */
    @RequestMapping("/reactivate")
    @Any
    public void reactivate(HttpServletRequest req) {
        if (sess.getActivateCode() == null)
            throw new ServiceError(ServiceError.ACTIVATE_CODE_NOT_EXISTS);
        long now = System.currentTimeMillis();
        if (now - sess.getActivateSendTime() < MINUTE1)
            throw new ServiceError(ServiceError.ACTIVATE_CODE_RESEND_TIME_TOO_SHORT);

        passwordService.reactivate(sess.getActivateCode(), "https://" + req.getServerName());
    }
}
