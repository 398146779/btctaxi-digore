package genesis.accounting.controller;

import genesis.accounting.domain.AccountingTransfer;
import genesis.accounting.enums.TransferSide;
import genesis.accounting.service.TransferService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/internal/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    /**
     * 列表
     * 1. fromAddress
     * 2. toAddress
     */
    @RequestMapping("/list")
    public Page<AccountingTransfer> list(@RequestParam("user_id") Long userId, @RequestParam TransferSide side,
                                         @RequestParam(required = false, defaultValue = "0") int page,
                                         @RequestParam(required = false, defaultValue = "100") Integer size) {

        return transferService.list(userId, side, page, size);

    }

    /**
     * 查询币种余额
     */
    @RequestMapping("/create")
    public Boolean query(@RequestParam Long from_user_id, @RequestParam Long to_user_id, @RequestParam String currency_name,
                         @RequestParam BigDecimal amount, @RequestParam(defaultValue = "0") BigDecimal fee, @RequestParam(required = false) String memo) {

        return transferService.create(from_user_id, to_user_id, currency_name, amount, fee, memo);
    }

    /**
     * 查询币种余额
     */
    @RequestMapping("/test")
    public Pair<String, Boolean> test() {
        return Pair.of("isSuccess", true);
    }


}
