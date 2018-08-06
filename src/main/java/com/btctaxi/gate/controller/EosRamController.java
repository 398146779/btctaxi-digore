package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.service.EosRamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate/eosram")

public class EosRamController extends BaseController {

    private Logger log = LoggerFactory.getLogger(EosRamController.class);
    EosRamService eosRamService;

    public EosRamController(EosRamService eosRamService) {
        this.eosRamService = eosRamService;
    }

    // 最小买入EOS数量为0.001EOS
    BigDecimal mineos = new BigDecimal("0.001");
    // 最小卖出RAM数量为0.01KB
    BigDecimal mineosram = new BigDecimal("0.01");

    /**
     * 需要系统配置crontab 任务
     */
    @RequestMapping("/timmerToEosRamPrice")
    @Any
    public void timmerToEosRamPrice() {
        eosRamService.timmerToEosRamPrice();
    }

    @RequestMapping("/getEosRamPrice")
    @Any
    public Map<String, BigDecimal> getEosRamPrice() {
        return eosRamService.getEosRamPrice();
    }


    @RequestMapping("/buyEosRam")
    public JSONObject buyEosRam(@RequestParam("eos_number") BigDecimal eos_number) {
        if (eos_number.compareTo(mineos) == -1) {
            return null;
        }
//        return eosRamService.buyEosRam(10726364L,eos_number);
        return eosRamService.buyEosRam(sess.getId(), eos_number);
    }

    @RequestMapping("/sellEosRam")
    public JSONObject sellEosRam(@RequestParam("eosram_number") BigDecimal eosram_number) {
        if (eosram_number.compareTo(mineosram) == -1) {
            return null;
        }
        return eosRamService.sellEosRam(sess.getId(), eosram_number);
    }

//    @RequestMapping("/sellEosRam")
//    public JSONObject sellEosRam(@RequestParam("eosram_number")BigDecimal eosram_number) {
//        return eosRamService.sellEosRam(sess.getId(),eosram_number);
//    }

    @RequestMapping("/getEosRamList")
    @Any
    public List<DataMap> getEosRamList(@RequestParam("size") Integer size) {
        size = size == null || size > 100 || size == 0 ? 10 : size;
        return eosRamService.getEosRamList(size);
    }

    @RequestMapping("/getEosRamListByUser")
    public List<DataMap> getEosRamListByUser(@RequestParam("size") Integer size) {
        size = size == null || size > 100 || size == 0 ? 10 : size;
        return eosRamService.getEosRamListByUser(sess.getId(), size);
    }
}
