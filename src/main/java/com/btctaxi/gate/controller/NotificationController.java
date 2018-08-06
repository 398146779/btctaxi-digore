package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONArray;
import genesis.gate.service.NotificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gate/notification")
public class NotificationController extends BaseController {

    private NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RequestMapping("/take")
    public Map<String, Object> take() {
        JSONArray notifications = notificationService.take(sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", notifications);
        return map;
    }
}
