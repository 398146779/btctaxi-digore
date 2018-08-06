package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.FavoritePairService;
import genesis.gate.service.PairService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gate/pair")
public class PairController extends BaseController {
    private PairService pairService;
    private FavoritePairService favoritePairService;

    public PairController(PairService pairService, FavoritePairService favoritePairService) {
        this.pairService = pairService;
        this.favoritePairService = favoritePairService;
    }

    /**
     * 交易对列表
     */
    @RequestMapping("/list")
    @Any
    public Map<String, Object> list(@RequestParam(name = "currency_name", required = false) String currencyName) {
        List<DataMap> pairs = pairService.list(currencyName);
        Map<String, Object> map = new HashMap<>();
        map.put("items", pairs);
        return map;
    }

    @RequestMapping("/query")
    @Any
    public DataMap query(@RequestParam(name = "pair_name") String pairName) {
        return pairService.query(pairName);
    }

    /**
     * 推荐交易对
     */
    @RequestMapping("/recommends")
    @Any
    public Map<String, Object> recommends() {
        List<DataMap> pairs = pairService.recommends();
        Map<String, Object> map = new HashMap<>();
        map.put("items", pairs);
        return map;
    }

    /**
     * 搜索
     */
    @RequestMapping("/search")
    @Any
    public Map<String, Object> search(@RequestParam(name = "text") String search_text) {
        List<DataMap> pairs = pairService.search(search_text);
        pairs.forEach(e -> e.put("is_favorite", 0));

        // 如果是登录用户，查看是否已收藏
        Long loginId = sess.getId();
        if (loginId > 0) {
            List<DataMap> favorList = favoritePairService.list(loginId);
            Map<String, Object> favorPairs = favorList.stream().collect(Collectors.toMap(e -> e.getString("pair_name"), e -> e));
            pairs.forEach(e -> {
                if (favorPairs.containsKey(e.getString("name"))) {
                    e.put("is_favorite", 1);
                }
            });
        }

        Map<String, Object> map = new HashMap<>();
        map.put("items", pairs);
        return map;
    }
}
