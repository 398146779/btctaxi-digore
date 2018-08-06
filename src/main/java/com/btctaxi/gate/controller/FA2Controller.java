package com.btctaxi.gate.controller;

import genesis.gate.service.FA2Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一FA处理
 */
@RestController
@RequestMapping("/gate/fa2")
public class FA2Controller extends BaseController {
    private FA2Service fa2Service;

    public FA2Controller(FA2Service fa2Service) {
        this.fa2Service = fa2Service;
    }

    @RequestMapping("/sms")
    @Any
    public void sms(@RequestParam String key) {
        fa2Service.sms(key);
    }

    @RequestMapping("/verify")
    @Any
    public Object verify(@RequestParam String type, @RequestParam String key, @RequestParam String code, HttpServletRequest req) {
        return fa2Service.verify(type, key, code, "https://" + req.getServerName(), sess.getKyc(), sess.getId());
    }
}
