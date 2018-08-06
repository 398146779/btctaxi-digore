package com.btctaxi.gate.config;

public interface RedisKeyConfig {
    String SYMBOL_PREFIX = "SYMBOL_";

    String COMMON_CONFIG = "COMMON_CONFIG";


    enum CommonConfig {
        LEADER_BOARD_OPEN, //排行榜开关
        REFUND_COMMISSION_RATE, //返佣 比例 有矿池前提反,TB 设置10%, TAXI:15%
        REFUND_COMMISSION_UNIT, //返佣 比例 TB BTC, TAXI: TAXI

        REFUND_LOCKE_RATE,   //返利锁量 比例, 100% , TB: 0, TAXI: 25%
        REFUND_LOCKE_UNIT,   //返利锁量 比例, 100% , TB: BTC, TAXI: TAXI

        REFUND_PROFIT_RATE, //反利 比例, TB: 设置10%,有矿池前提反10%, TAXI:有邀请的20%
        REFUND_PROFIT_UNIT, //反利 比例  TB: BTC, TAXI: TAXI

        DIVIDEND_RATE, // 分红比例 TB:0 , TAXI:10%
        DIVIDEND_UNIT, //

        MINE_RATE,//挖旷 比例 TB: 设置10%, TAXI: 设置100%
        MINE_UNIT,// 挖矿 币种 TB: BTC, TAXI: 平台币

        THINK_BIT,//平台币?

        USER_UP_LIMIT, //挖矿上限 每天5W


//        REFUND_COMMISSION_RATE, //返佣 比例  0.015 ==
//        REFUND_COMMISSION_UNIT, //返佣 比例  IOST
//        REFUND_LOCKE_RATE,   //返利 比例,  0.25
//        REFUND_LOCKE_UNIT,   //返利 比例, IOST
//        REFUND_PROFIT_RATE, //反利润 比例,  0.05 ==
//        REFUND_PROFIT_UNIT, //反利润 比例  IOST
//        DIVIDEND_RATE, // 分红比例  0.51 == 矿池所有人额外增加0.015???
//        MINE_RATE,//挖旷 比例 1.15 ==
//        MINE_UNIT ,// 挖矿 币种 IOST
//        THINK_BIT,//平台币 IOST

    }
}
