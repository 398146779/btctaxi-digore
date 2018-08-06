package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.RegionService;
import genesis.gate.util.IP;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/gate/region")
public class RegionController extends BaseController {
    private RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @RequestMapping("/list")
    @Any
    public List<DataMap> regions(HttpServletRequest req) {
        List<DataMap> regions = regionService.list();

        long ip = IP.ip2long(IP.getIpAddr(req));
        DataMap map = regionService.getRegion(ip);
        int code = map == null || map.getInt("code") == 0 ? 86 : map.getInt("code");
        regions.forEach(region ->
        {
            if (code == region.getInt("id"))
                region.put("selected", true);
        });
        return regions;
    }
}
