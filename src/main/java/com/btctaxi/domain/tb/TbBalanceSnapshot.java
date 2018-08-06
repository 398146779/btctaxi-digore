package com.btctaxi.domain.tb;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: guangtou
 * Date: 2018/7/14 22:05
 */
@Entity
@Data
@Builder
@Table(name = "tb_balance_snapshot")
public class TbBalanceSnapshot {
    private int id;
    @Basic
    @Column(name = "uid")
    private long uid;
    private String currency;
    private BigDecimal accountBalance;

    @Basic
    @Column(name = "create_time")
    @CreatedDate
    private Date createTime;

    @Id
    @Column(name = "id")
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Basic
    @Column(name = "currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Basic
    @Column(name = "account_balance")
    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }




}
