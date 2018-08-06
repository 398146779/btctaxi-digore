package genesis.accounting.controller;

import genesis.accounting.service.WithdrawService;
import genesis.common.DataMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    private WithdrawService withdrawService;

    public WithdrawController(WithdrawService withdrawService) {
        this.withdrawService = withdrawService;
    }

    /**
     * 检查充币入账进度
     */
    @RequestMapping("/check")
    public void check() {
        withdrawService.check();
    }

    /**
     * 查询用户提币额度
     */
    @RequestMapping("/quota/query")
    public DataMap queryQuota(@RequestParam("user_id") Long userId) {
        return withdrawService.queryQuota(userId);
    }

    /**
     * 更新用户可用额度
     */
    @RequestMapping("/quota/update")
    public void updateQuota(@RequestParam("user_id") Long userId, @RequestParam BigDecimal amount) {
        withdrawService.updateQuota(userId, amount);
    }

    /**
     * 发起提币
     */
    @RequestMapping("/create")
    public void create(@RequestParam("user_id") Long userId, @RequestParam("chain_name") String chainName,
                       @RequestParam("currency_name") String currencyName, @RequestParam String address,
                       @RequestParam(required = false) String memo, @RequestParam BigDecimal amount) {
        withdrawService.create(userId, chainName, currencyName, address, memo, amount);
//        reviewService.applyWithdraw(userId, currencyName, chainName, amount, address, memo);
    }

    /**
     * 取消提币
     */
    @RequestMapping("/remove")
    public void remove(@RequestParam("user_id") Long userId, @RequestParam Long id) {
        withdrawService.remove(userId, id);
    }

    /**
     * 查询最近提币记录
     */
    @RequestMapping("/list")
    public List<DataMap> list(@RequestParam("user_id") Long userId) {
        return withdrawService.list(userId);
    }

    /**
     * 查询是近一笔转账信息
     */
    @RequestMapping("/latest")
    public DataMap latest(@RequestParam("user_id") Long userId, @RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName, @RequestParam String address, @RequestParam(required = false) String memo) {
        return withdrawService.latest(userId, chainName, currencyName, address, memo);
    }
}
