package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.FavoritePairService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate/favorite")
public class FavoritePairController extends BaseController {
    private FavoritePairService favoritePairService;

    public FavoritePairController(FavoritePairService favoritePairService) {
        this.favoritePairService = favoritePairService;
    }

    @RequestMapping("/list")
    public Map<String, Object> list() {
        List<DataMap> pairs = favoritePairService.list(sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", pairs);
        return map;
    }

    @RequestMapping("/create")
    public void create(@RequestParam("pair_name") String pairName) {
        favoritePairService.create(sess.getId(), pairName);
    }

    @RequestMapping("/remove")
    public void remove(@RequestParam("pair_name") String pairName) {
        favoritePairService.remove(sess.getId(), pairName);
    }
}
