package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.BannerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate/banner")
public class BannerController extends BaseController {
    private BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @RequestMapping("/list")
    @Any
    public Map<String, Object> list(@RequestParam Integer platform, @RequestParam Integer slot) {
        List<DataMap> banners = bannerService.list(platform, slot, sess.getLocale());
        Map<String, Object> map = new HashMap<>();
        map.put("items", banners);
        return map;
    }
}
