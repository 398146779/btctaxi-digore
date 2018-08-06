package genesis.accounting.controller;

import genesis.accounting.service.CurrencyService;
import genesis.common.DataMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/currency")
public class CurrencyController {
    private CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * 交易所上线的币种列表
     */
    @RequestMapping("/list")
    public List<DataMap> list() {
        return currencyService.list();
    }

    /**
     * 查询币种详情
     */
    @RequestMapping("/query")
    public List<DataMap> query(@RequestParam("currency_name") String currencyName) {
        return currencyService.query(currencyName);
    }
}
