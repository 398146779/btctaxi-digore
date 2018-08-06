package genesis.accounting.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import genesis.accounting.controller.support.ErrorCodeEnum;
import genesis.accounting.controller.support.ServiceError;
import genesis.accounting.dao.AccountingArchiveRepository;
import genesis.accounting.domain.AccountingArchive;
import genesis.accounting.service.AccountingArchiveService;
import genesis.accounting.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static genesis.accounting.service.AccountingArchiveService.*;

/**
 * User: guangtou
 * Date: 2018/6/27 14:47
 */
@Slf4j
@RestController
@RequestMapping("/archive/v1")
public class AccountingArchiveController {

    @Autowired
    AccountingArchiveService accountingArchiveService;
    @Autowired
    AccountingArchiveRepository accountingArchiveRepository;

    //归集
    @RequestMapping("/create")
    public void create(@RequestParam(required = false)  String dateStr) {
        Date date = null;
        try {
            if (StringUtils.isNotEmpty(dateStr) ) {
                date = FastDateFormat.getInstance("yyyy-MM-dd").parse(dateStr);
            }
        } catch (ParseException e) { }


        if (date == null ){
            date = DateTime.now().plusDays(-10).toDate();
        }
        Date finalDate = date;

        accountingArchiveService.create(ETH, ETH, finalDate);
        accountingArchiveService.create(OMNI, BTC, finalDate);

    }


    @RequestMapping("/get")
    @ResponseBody
    public AccountingArchive getData(@RequestParam Long id) {
        AccountingArchive one = accountingArchiveRepository.findOne(id);
        one.setWalletTxId(accountingArchiveService.getIdSha256HexString(one));
        return one;
    }


    //归集： 列表 （一审、二审、重审， 发送中，异常）； 一审接口，二审接口
    //矿工费： 列表 （一审、二审、重审， 发送中，异常）； 一审接口，二审接口

    //归集
    @RequestMapping("/listByStates")
    public Page<AccountingArchive> listByStates(@RequestParam String states,
                                                @RequestParam Integer page,
                                                @RequestParam Integer size) {
        List<Integer> list = Lists.newArrayList(states.split(","))
                .stream().map(Integer::parseInt)
                .collect(Collectors.toList());

        Page<AccountingArchive> archives = accountingArchiveRepository.findAllByStateInOrderByTxTypeAscCreateTimeAsc(new PageRequest(page, size), list);
        archives.getContent().forEach(archive -> {
            archive.setWalletTxId(accountingArchiveService.getIdSha256HexString(archive));
        });
        return archives;
    }


    /**
     * putReview1
     */
    @RequestMapping("/putReview1")
    @ResponseBody
    public Boolean putReview1(@RequestParam Long id,
                              @RequestParam(required = false) String walletTxId,
                              @RequestParam Integer state,
//                         @RequestParam TbWithdraw.State state,
                              @RequestParam Long reviewerId,
                              @RequestParam String address,
                              @RequestParam(required = false) String changeAddress,
                              @RequestParam(required = false) Long reviewerNonce,
                              @RequestParam(required = false) String reviewerSignature) {
        log.info("review: id={}, state={}, walletTxId=hex={}, reviewId={}, reviewerNonce={}, reviewerSignature={}, fromAddress={}, changeAddress={}",
                id, state, walletTxId, reviewerId, reviewerNonce, reviewerSignature, address, changeAddress);
        if (!ReviewService.oaAllowStateList.contains(state)) {
            //state is no allow
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!ReviewService.review1StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        return accountingArchiveService.review1(id, state, reviewerId, reviewerNonce, reviewerSignature, address, changeAddress);
    }

    /**
     * putReview2
     */
    @RequestMapping("/putReview2")
    @ResponseBody
    public Boolean putReview2(@RequestParam Long id,
                              @RequestParam String walletTxId,
                              @RequestParam Integer state,
//                         @RequestParam TbWithdraw.State state,
                              @RequestParam Long reviewerId,
                              @RequestParam(required = false) Long reviewerNonce,
                              @RequestParam(required = false) String reviewerSignature) {
        log.info("review: id={}, state={},  reviewId={}, reviewerNonce={}, reviewerSignature={} ",
                id, walletTxId, state, reviewerId, reviewerNonce, reviewerSignature);

        if (!ReviewService.oaAllowStateList.contains(state)) {
            //state is no allow
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!ReviewService.review2StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }
        return accountingArchiveService.review2(id, state, reviewerId, reviewerNonce, reviewerSignature);
    }

    /**
     * JOB 同步状态
     */
    @RequestMapping("/transferToSuccess")
    @ResponseBody
    public void transferToSuccess() {
        accountingArchiveService.checkWithdrawStatus();
    }




    /**
     * 查询钱包 tx
     *
     * @param id
     * @return
     */
    @RequestMapping("/getWalletTransaction")
    @ResponseBody
    public JSONObject getWalletTransaction(@RequestParam Long id) {
        AccountingArchive archive = accountingArchiveRepository.getOne(id);
        return accountingArchiveService.getWalletTransaction(archive);
    }

}
