package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import genesis.gate.service.AddressService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate/address")
public class AddressController extends BaseController {
    private AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 查询充币地址
     */
    @RequestMapping("/deposit/query")
    public JSONObject depositQuery(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName) {
        return addressService.depositQuery(sess.getId(), chainName, currencyName);
    }

    /**
     * 提币地址列表
     */
    @RequestMapping("/withdraw/list")
    public Map<String, Object> withdrawList(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName) {
        List<DataMap> addrs = addressService.withdrawList(sess.getId(), chainName, currencyName);
        Map<String, Object> map = new HashMap<>();
        map.put("items", addrs);
        return map;
    }

    /**
     * 创建新的一条提币地址
     */
    @RequestMapping("/withdraw/create")
    public DataMap create(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName, @RequestParam String label, @RequestParam String address, @RequestParam(required = false) String memo) {
        return addressService.create(sess.getId(), chainName, currencyName, label, address, memo);
    }

    /**
     * 删除一条提币地址
     */
    @RequestMapping("/withdraw/remove")
    public void remove(@RequestParam Long id) {
        addressService.remove(sess.getId(), id);
    }
}
