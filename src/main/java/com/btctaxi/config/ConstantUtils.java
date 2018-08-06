package com.btctaxi.config;

public class ConstantUtils {
    /**查询EOSRAM价格*/
    public static String GETEOSRAM = "/eos/ram/price";
    /**购买EOS RAM*/
    public static String BUYEOSRAM = "/eos/ram/buy";
    /**卖出EOS RAM*/
    public static String SELLEOSRAM = "/eos/ram/sell";

    public static String getId(){
        return (Math.random()+"").substring(2,16);
    }

}
