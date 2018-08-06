package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.service.NoticeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** */
@RestController
@RequestMapping("/gate/notice")
public class NoticeController extends BaseController {
    private NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 公告列表
     *
     * @return
     */
    @RequestMapping("/list")
    @Any
    public Map<String, Object> list(@RequestParam Integer platform, @RequestParam Integer slot) {
        List<DataMap> notices = noticeService.list(platform, slot, sess.getLocale());
        Map<String, Object> map = new HashMap<>();
        map.put("items", notices);
        return map;
    }
}
