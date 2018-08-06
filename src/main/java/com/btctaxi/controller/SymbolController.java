package genesis.accounting.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import genesis.accounting.service.SymbolService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/symbol")
public class SymbolController {
    private SymbolService symbolService;

    public SymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    /**
     * Symbol列表
     */
    @RequestMapping("/list")
    public JSONArray list() {
        return symbolService.list();
    }

    /**
     * 查询Symbol
     */
    @RequestMapping("/query")
    public JSONObject query(@RequestParam("currency_name") String currencyName) {
        return symbolService.query(currencyName);
    }
}
