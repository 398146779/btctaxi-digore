package com.btctaxi.controller;

import com.alibaba.fastjson.JSONObject;
import com.btctaxi.service.EosRamService;
import com.btctaxi.common.DataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/eosram/v1")
public class EosRamController {
    @Autowired
    private EosRamService eosRamService;

    @RequestMapping("/getEosRamPrice")
    public JSONObject getEosRamPrice(){
        return eosRamService.getEosRamPrice();
    }

    @RequestMapping("/buyEosRam")
    public JSONObject buyEosRam(@RequestParam("user_id")Long userId, @RequestParam("eos_number")BigDecimal eos_number) {
        return eosRamService.buyEosRam(userId,eos_number);
    }

    @RequestMapping("/sellEosRam")
    public JSONObject sellEosRam(@RequestParam("user_id")Long userId, @RequestParam("eosram_number")BigDecimal eosram_number) {
        return eosRamService.sellEosRam(userId,eosram_number);
    }

    @RequestMapping("/getEosRamListByUser")
    public List<DataMap> getEosRamListByUser(@RequestParam("user_id") Long user_id, @RequestParam("size")Integer size) {
        return eosRamService.getEosRamList(user_id,size);
    }

    @RequestMapping("/getEosRamList")
    public List<DataMap> getEosRamList(@RequestParam("size")Integer size) {
        return eosRamService.getEosRamList(size);
    }


}
