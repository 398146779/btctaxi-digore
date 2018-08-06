package genesis.accounting.controller;

import genesis.accounting.service.DepositService;
import genesis.common.DataMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deposit")
public class DepositController {
    private DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    /**
     * 检查充币入账进度
     */
    @RequestMapping("/check")
    public void check(@RequestParam(required = false) Long id, @RequestParam(required = false) Integer size) {
        depositService.check(id, size);
    }

    /**
     * 查询最近充币记录
     */
    @RequestMapping("/list")
    public List<DataMap> list(@RequestParam("user_id") Long userId) {
        return depositService.list(userId);
    }
}
