package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONArray;
import genesis.common.DataMap;
import genesis.gate.service.TransferService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gate/transfer")
public class TransferController extends BaseController {
    private TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @RequestMapping("/list")
    public DataMap list() {
        DataMap map = new DataMap();
        JSONArray transfers = transferService.list(sess.getId());
        map.put("items", transfers);
        return map;
    }

    @RequestMapping("/recent")
    public JSONArray recent() {
        return transferService.recent(sess.getId());
    }
}
