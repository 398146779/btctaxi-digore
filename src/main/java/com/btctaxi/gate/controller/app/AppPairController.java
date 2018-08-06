package com.btctaxi.gate.controller.app;

import genesis.common.DataMap;
import genesis.gate.controller.PairController;
import genesis.gate.service.FavoritePairService;
import genesis.gate.service.PairService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/pair")
public class AppPairController extends PairController {
    private PairService pairService;

    public AppPairController(PairService pairService, FavoritePairService favoritePairService) {
        super(pairService, favoritePairService);
        this.pairService = pairService;
    }

    @RequestMapping("/group")
    @Any
    public Map<String, Object> group() {
        List<Map<String, Object>> groups = pairService.group(sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", groups);
        return map;
    }

    @RequestMapping("/chartBusiness")
    public Pair<String, Boolean> chartBusiness(@RequestParam String pair_name) {
        List<DataMap> dataMapList = pairService.havePair(sess.getId(), pair_name);
        return Pair.of("isfavorite", dataMapList.size() > 0);

    }


}
