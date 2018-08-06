package com.btctaxi.gate.controller.app;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.controller.BaseController;
import genesis.gate.service.BannerService;
import genesis.gate.service.MarketService;
import genesis.gate.service.NoticeService;
import genesis.gate.service.PairService;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app/index")
public class IndexController extends BaseController {
    private BannerService bannerService;
    private PairService pairService;
    private NoticeService noticeService;
    private MarketService marketService;

    public IndexController(BannerService bannerService, PairService pairService, NoticeService noticeService, MarketService marketService) {
        this.bannerService = bannerService;
        this.pairService = pairService;
        this.noticeService = noticeService;
        this.marketService = marketService;
    }

    @RequestMapping("/list")
    @Any
    public DataMap list(@RequestParam Integer platform, @RequestParam Integer slot, @RequestHeader(value = "lang", required = false, defaultValue = "en") String lang) {
        List<DataMap> banners = bannerService.list(platform, slot, lang);
        List<DataMap> recommends = pairService.recommends();
        List<DataMap> notices = noticeService.list(platform, slot, sess.getLocale());

        List<JSONObject> tickersAll = marketService.tickerAll(); // 行情信息
        List<JSONObject> topGainers = marketService.topGainers(tickersAll, 10);
        List<JSONObject> topHots = marketService.topHots(tickersAll, 10);

        // 将当前行情信息加到recommends里
        if (!recommends.isEmpty()) {

            Map<String, JSONObject> tickersMap = tickersAll.stream().collect(Collectors.toMap(e -> e.getString("pair"), e -> e));

            recommends.forEach(e -> {
                String name = e.getString("name");
                e.put("pair", name);
                if(tickersMap.containsKey(name))
                    e.putAll(tickersMap.get(name));
            });
        }

        DataMap map = new DataMap();
        map.put("banners", banners);
        map.put("recommends", recommends);
        map.put("hots", topHots);
        map.put("notices", notices); // 公告
        map.put("top_gainers", topGainers); // 24小时交易榜
        return map;
    }

    // TODO 获取热门交易对
    public List<DataMap> hotList() {
        List<DataMap> hots = new ArrayList<>();
        DataMap map = new DataMap();
        map.put("name", "ETH_BTC");
        map.put("product_name", "ETH");
        map.put("currency_name", "BTC");
        hots.add(map);
        return hots;
    }
}
