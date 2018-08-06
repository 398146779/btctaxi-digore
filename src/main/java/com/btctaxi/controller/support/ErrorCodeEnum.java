package com.btctaxi.controller.support;


public enum ErrorCodeEnum implements com.btctaxi.controller.support.EnumBehaviour {
    SERVER_ERROR(500, "服务器繁忙"),

    PARAMS_ERROR(1001, "参数异常"),
    PARAMS_ERROR_NOTNULL(1002, "参数异常"),
    DATA_NOT_FOND(1003, "参数异常"),

    OPERATION_NOT_ALLOW(2002, "operation is no allow"),
    FINAL_STATE_NOT_MODIFIED(2003, "终极状态不能修改, Final state cannot be modified !"),
    NON_CURRENT_USER_DATA(2004, "不匹配: id and walletTxId not match !"),
    REVIEW2_NOT_CANCEL(2015, "二审无法拒绝"),
    CANCEL_FAILED(2003, "成功调用钱包,但钱包撤销失败"),
    CALL_CANCEL_FAILED(2006, "调用钱包撤销请求失败"),
    IRREVERSIBLE_STATE(2007, "状态不可逆, Irreversible state !"),
    CALL_REVIEW1_FAILED(2008, "调用钱包 create1 请求失败"),
    CALL_REVIEW2_FAILED(2009, "调用钱包 create2 请求失败"),
    CALL_TRANSACTION_FAILED(2010, "调用钱包 transaction 请求失败"),
    PRE_CALL_REVIEW1(2011, "先调用一审接口, please call review1 interface !"),
    APPLY_REFUSED_FAILED(2012, "转账中无法撤销"),
    GAS_NOT_ENOUGH(20013, "矿工费不足, gas not enough"),


    TRANSFER_BALANCE_NOT_ENOUGH(4001, "balance not enough"),


    WITHDRAW_FORBIDDEN_24_HOURS(3004, "转账中无法撤销");





    private final int value;
    private final String desc;

    ErrorCodeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}