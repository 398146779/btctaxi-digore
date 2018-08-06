package com.btctaxi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * _user: guangtou
 * _date: 2018/6/7 21:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {
    private Long user_id;
    private String currency_name; // 币名

    private BigDecimal available;//可用量
    private BigDecimal withdrawing; //提币中量
    private BigDecimal ordering; //订单锁定量
    private BigDecimal locking; //业务逻辑锁定量


}
