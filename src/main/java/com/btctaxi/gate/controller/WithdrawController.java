package com.btctaxi.gate.controller;

import genesis.gate.error.ServiceError;
import genesis.gate.service.WithdrawService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/gate/withdraw")
public class WithdrawController extends BaseController {

    private WithdrawService withdrawService;

    public WithdrawController(WithdrawService withdrawService) {
        this.withdrawService = withdrawService;
    }

    /**
     * 提币请求
     */
    @RequestMapping("/create")
    public Map<String, Object> create(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName, @RequestParam BigDecimal amount, @RequestParam String address, @RequestParam(required = false) String memo, HttpServletRequest req) {
        if (sess.getGoogleKey() == null && sess.getPhone() == null)
            throw new ServiceError(ServiceError.FA2_BIND_NEEDED);
        return withdrawService.create1(sess.getId(), chainName, currencyName, address, memo, amount, sess.getEmail(), sess.getNick(), sess.getPhone(), sess.getGoogleKey(), "https://" + req.getServerName());
    }

    /**
     * 邮件确认
     */
    @RequestMapping("/confirm")
    @Any
    public void confirm(@RequestParam String auth) {
        withdrawService.create3(auth);
    }

    /**
     * 取消提币
     */
    @RequestMapping("/cancel")
    public void remove(@RequestParam Long id) {
        withdrawService.remove(sess.getId(), id);
    }

    /**
     * 生发邮件
     */
    @RequestMapping("/resend")
    public void resend() {
        withdrawService.resend(sess.getId());
    }
}
