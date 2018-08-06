package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.btctaxi.gate.config.GeetestConfig;
import com.btctaxi.gate.vendor.GeetestLib;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@RequestMapping("/gate/geetest")
@Slf4j
public class GeetestController extends BaseController {

    private GeetestConfig geetestConfig;

    public GeetestController(GeetestConfig geetestConfig) {
        this.geetestConfig = geetestConfig;
    }

    @RequestMapping("/begin")
    @Any
    public JSONObject start(HttpServletRequest req) {
        GeetestLib gtSdk = new GeetestLib(geetestConfig.getId(), geetestConfig.getKey(), geetestConfig.isNewFailBack());
        HashMap<String, String> map = new HashMap<>();
        map.put("ip_address", req.getRemoteAddr());

        int gtServerStatus = gtSdk.preProcess(map);

        log.info("geetest begin: {}", gtServerStatus);

        sess.setGtServerStatus(gtServerStatus);

        String resStr = gtSdk.getResponseStr();
        return JSON.parseObject(resStr);
    }
}
