package com.btctaxi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SumBalance {
    private BigDecimal fee_tb;
    private BigDecimal fee_currency;
    private BigDecimal fee_product;
    private Long user_id;
    private Long order_id;
    private Date create_time;
    private BigDecimal price;
    private BigDecimal amount;
}