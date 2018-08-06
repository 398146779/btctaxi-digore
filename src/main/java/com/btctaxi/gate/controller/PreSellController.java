package com.btctaxi.gate.controller;

import genesis.gate.service.PreSellService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/gate/presell")
public class PreSellController extends BaseController {

    private PreSellService preSellService;

    public PreSellController(PreSellService preSellService) {
        this.preSellService = preSellService;
    }

    @RequestMapping("/query")
    public Map<String, Object> query() {
        return preSellService.query(sess.getId());
    }

    @RequestMapping("/lock")
    public void lock(@RequestParam Long amount) {
        long userId = sess.getId();
        preSellService.lock(amount, userId);
    }


//    @RequestMapping("/refund")
//    public void refund(@RequestParam(required = false) String day) {
//        long userId = sess.getId();
//        if (StringUtils.isEmpty(day)){
//            day = FastDateFormat.getInstance("yyyy-MM-dd").format(new Date());
//        }
//
//        preSellService.refund(day, userId);
//    }
}
