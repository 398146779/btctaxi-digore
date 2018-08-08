package com.btctaxi.controller;

import com.btctaxi.service.AddressService;
import com.btctaxi.common.DataMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController {
    private AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 获取用户充币地址
     */
    @RequestMapping("/query")
    public DataMap query(@RequestParam("user_id") Long userId, @RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName) {
        return addressService.query(userId, chainName, currencyName);
    }

    /**
     * 获取用户ID
     */
    @RequestMapping("/userId")
    public DataMap queryUserId(@RequestParam("chain_name") String chainName, @RequestParam("address") String address) {
        return addressService.queryUserId(chainName, address);
    }
}
