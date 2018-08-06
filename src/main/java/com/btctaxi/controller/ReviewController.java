package genesis.accounting.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import genesis.accounting.controller.support.ErrorCodeEnum;
import genesis.accounting.controller.support.ServiceError;
import genesis.accounting.domain.Withdraw;
import genesis.accounting.service.ReviewService;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/review/v1")
@Slf4j
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

//    /**
//     * 提币请求列表
//     */
//    @RequestMapping("/request/list")
//    public List<DataMap> listRequest() {
//        return reviewService.listRequest();
//    }
//
//    /**
//     * 一审结果列表
//     */
//    @RequestMapping("/review1/list")
//    public List<DataMap> listReview1() {
//        return reviewService.listReview1();
//    }
//
//    /**
//     * 二审结果列表
//     */
//    @RequestMapping("/review2/list")
//    public List<DataMap> listReview2() {
//        return reviewService.listReview2();
//    }
//
//    /**
//     * 查询提币请求
//     */
//    @RequestMapping("/query")
//    public DataMap query(@RequestParam Long id) {
//        return reviewService.query(id);
//    }
//
//    /**
//     * 公积金地址列表
//     */
//    @RequestMapping("/address/list")
//    public List<DataMap> listAddress(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName) {
//        return reviewService.listAddress(chainName, currencyName);
//    }
//
//    /**
//     * 一审
//     */
//    @RequestMapping("/review1")
//    public void review1(@RequestParam Long id, @RequestParam("user_id") Long userId, @RequestParam("reviewer1_id") Long reviewer1Id, @RequestParam("reviewer1_nonce") Long reviewer1Nonce, @RequestParam("reviewer1_signature") String reviewer1Signature, @RequestParam("from_address") String fromAddress) {
//        reviewService.review1(id, userId, reviewer1Id, reviewer1Nonce, reviewer1Signature, fromAddress);
//    }
//
//    /**
//     * 二审
//     */
//    @RequestMapping("/review2")
//    public void review2(@RequestParam Long id, @RequestParam("user_id") Long userId, @RequestParam("reviewer2_id") Long reviewer2Id, @RequestParam("reviewer2_nonce") Long reviewer2Nonce, @RequestParam("reviewer2_signature") String reviewer2Signature) {
//        reviewService.review2(id, userId, reviewer2Id, reviewer2Nonce, reviewer2Signature);
//    }
//
//    /**
//     * 拒绝
//     */
//    @RequestMapping("/refuse")
//    public void refuse(@RequestParam Long id, @RequestParam("user_id") Long userId) {
//        reviewService.refuse(id, userId);
//    }


    /**
     * //     * 公积金地址列表
     * //
     */
    @RequestMapping("/address/list")
    public List<DataMap> listAddress(@RequestParam("chain_name") String chainName, @RequestParam("currency_name") String currencyName) {

        return reviewService.listAddress(chainName, currencyName);
    }

    /*******************************/

    /**
     * 拉取提币申请 list
     *
     * @return
     */
    @RequestMapping("/listByStates")
    @ResponseBody
    public Page<Withdraw> listByStates(@RequestParam String states,
                                       @RequestParam Integer page,
                                       @RequestParam Integer size) {

        //check states in enumsList

        List<Integer> list = Lists.newArrayList(states.split(","))
                .stream().map(Integer::parseInt)
                .collect(Collectors.toList());
        //TODO toVo obj

        List<Withdraw> list1 = reviewService.findAllWithdrawByState(list, page, size);
        return new PageImpl<>(list1);
    }

    @RequestMapping("/get")
    @ResponseBody
    public Withdraw getData(@RequestParam Long id,
                            @RequestParam Long user_id) {
        return reviewService.findByIdAndUserId(id, user_id);
    }

    /**
     * 拉取提币申请 record
     */
    @RequestMapping("/listByIdAndUserId")
    public Withdraw listByIdAndUserId(@RequestParam Long id,
                                      @RequestParam Long user_id,
                                      @RequestParam Integer page,
                                      @RequestParam Integer size) {
//        Preconditions.checkArgument(size < 50);
//        Preconditions.checkArgument(size < 1000);
        return reviewService.listByIdAndUserId(id, user_id, page, size);
    }

    /**
     * putReview1
     */
    @RequestMapping("/putReview1")
    @ResponseBody
    public Boolean putReview1(@RequestParam Long id,
                              @RequestParam Long user_id,
                              @RequestParam(required = false) String walletTxId,
                              @RequestParam Integer state,
//                         @RequestParam TbWithdraw.State state,
                              @RequestParam Long reviewerId,
                              @RequestParam String fromAddress,
                              @RequestParam(required = false) String changeAddress,
                              @RequestParam(required = false) Long reviewerNonce,
                              @RequestParam(required = false) String reviewerSignature) {
        log.info("review: id={}, state={}, walletTxId=hex={}, reviewId={}, reviewerNonce={}, reviewerSignature={}, fromAddress={}, changeAddress={}",
                id, state, walletTxId, reviewerId, reviewerNonce, reviewerSignature, fromAddress, changeAddress);
        if (!reviewService.oaAllowStateList.contains(state)) {
            //state is no allow
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!reviewService.review1StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        return reviewService.review1(id, user_id, state, reviewerId, reviewerNonce, reviewerSignature, fromAddress, changeAddress);
    }


    /**
     * putReview2
     */
    @RequestMapping("/putReview2")
    @ResponseBody
    public Boolean putReview2(@RequestParam Long id,
                              @RequestParam Long user_id,
                              @RequestParam String walletTxId,
                              @RequestParam Integer state,
//                         @RequestParam TbWithdraw.State state,
                              @RequestParam Long reviewerId,
                              @RequestParam(required = false) Long reviewerNonce,
                              @RequestParam(required = false) String reviewerSignature) {
        log.info("review: id={}, state={},  reviewId={}, reviewerNonce={}, reviewerSignature={} ",
                id, walletTxId, state, reviewerId, reviewerNonce, reviewerSignature);

        if (!reviewService.oaAllowStateList.contains(state)) {
            //state is no allow
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }

        if (!reviewService.review2StateList.contains(state)) {
            ServiceError.ofAndThrow(ErrorCodeEnum.OPERATION_NOT_ALLOW);
        }
        return reviewService.review2(id, user_id, state, reviewerId, reviewerNonce, reviewerSignature);
    }


    /**
     * 撤销
     *
     * @param id
     * @param userId
     * @return
     */
    @RequestMapping("/cancel")
    @ResponseBody
    public Boolean cancel(@RequestParam Long id,
                          @RequestParam Long user_id) {
        reviewService.walletCancelService(id, user_id);
        return true;
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
        return reviewService.getWalletTransaction(id);
    }


    /**
     * JOB 同步状态
     */
    @RequestMapping("/transferToSuccess")
    @ResponseBody
    public void transferToSuccess() {
        reviewService.checkWithdrawStatus();
    }


    /**
     * 提币列表
     *
     * @param userId
     * @return
     */
    @RequestMapping("/list")
    public List<Withdraw> list(@RequestParam Long user_id, @RequestParam String states) {
        List<Integer> list = Lists.newArrayList(states.split(","))
                .stream().map(Integer::parseInt)
                .collect(Collectors.toList());
        return reviewService.list(user_id, list);
    }


    /**
     * 提币列表
     *
     * @param userId
     * @return
     */
    @RequestMapping("/listForPage")
    public Page<Withdraw> listForPage(@RequestParam Long user_id, @RequestParam String states) {
        List<Integer> list = Lists.newArrayList(states.split(","))
                .stream().map(Integer::parseInt)
                .collect(Collectors.toList());
        List<Withdraw> list1 = reviewService.list(user_id, list);
        return new PageImpl<>(list1);

    }

//    /**
//     * 创建提币请求
//     *
//     * @param userId
//     * @param currencyName
//     * @param amount
//     */
//    @RequestMapping("/create")
//    public void create(@RequestParam Long user_id,
//                       @RequestParam String currencyName,
//                       @RequestParam String chainName,
//                       @RequestParam BigDecimal amount,
//                       @RequestParam String toAddress,
//                       @RequestParam(required = false) String memo) {
//        reviewService.applyWithdraw(user_id, currencyName, chainName, amount, toAddress, memo);
//    }

//    /**
//     * 取消 提币 请求
//     *
//     * @param userId
//     * @param withdrawId
//     */
//    @RequestMapping("/abort")
//    public void abort(@RequestParam Long user_id, @RequestParam Long withdrawId) {
//        reviewService.cancel(user_id, withdrawId);
//
//    }

}
