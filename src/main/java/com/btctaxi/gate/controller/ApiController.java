package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.ApiService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate/api")
public class ApiController extends BaseController {
    private ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @RequestMapping("/permissions")
    public Map<String, Object> permissions() {
        List<DataMap> permissions = apiService.permissions();
        Map<String, Object> map = new HashMap<>();
        map.put("items", permissions);
        return map;
    }

    @RequestMapping("/create")
    public Map<String, Object> create(@RequestParam long permission, @RequestParam String label, HttpServletRequest req) {
        return apiService.create(permission, label, "https://" + req.getServerName());
    }

    @RequestMapping("/create/confirm")
    public Map<String, Object> create_confirm(@RequestParam String auth) {
        return apiService.create_confirm(auth);
    }

    @RequestMapping("/list")
    public Map<String, Object> list() {
        List<DataMap> apis = apiService.list(sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", apis);
        return map;
    }

    @RequestMapping("/update")
    public Map<String, Object> update(@RequestParam("api_id") long apiId, @RequestParam long permission) {
        return apiService.update(apiId, permission);
    }

    @RequestMapping("/remove")
    public void remove(@RequestParam("api_id") long apiId) {
        apiService.remove(apiId, sess.getId());
    }
}
