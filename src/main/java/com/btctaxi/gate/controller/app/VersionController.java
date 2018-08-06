package com.btctaxi.gate.controller.app;

import genesis.common.DataMap;
import genesis.gate.controller.BaseController;
import genesis.gate.service.VersionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController extends BaseController {
    private VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @RequestMapping("/app/version")
    @Any
    public DataMap version(@RequestParam String plat) {
        return versionService.version(plat);
    }
}
