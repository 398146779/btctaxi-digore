package com.btctaxi.domain.tb;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * User: guangtou
 * Date: 2018/7/5 17:35
 */
@Entity
@Table(name = "tb_transaction")
@NoArgsConstructor
@IdClass(TbTransactionPK.class)
public class TbTransaction {
    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal price;
    private BigDecimal amount;
    private Timestamp createTime;
    private BigDecimal feeTb;
    private BigDecimal feeCurrency;
    private BigDecimal feeProduct;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Id
    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Basic
    @Column(name = "price")
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Basic
    @Column(name = "amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Id
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "fee_tb")
    public BigDecimal getFeeTb() {
        return feeTb;
    }

    public void setFeeTb(BigDecimal feeTb) {
        this.feeTb = feeTb;
    }

    @Basic
    @Column(name = "fee_currency")
    public BigDecimal getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(BigDecimal feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    @Basic
    @Column(name = "fee_product")
    public BigDecimal getFeeProduct() {
        return feeProduct;
    }

    public void setFeeProduct(BigDecimal feeProduct) {
        this.feeProduct = feeProduct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbTransaction that = (TbTransaction) o;

        if (id != that.id) return false;
        if (userId != that.userId) return false;
        if (orderId != that.orderId) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (feeTb != null ? !feeTb.equals(that.feeTb) : that.feeTb != null) return false;
        if (feeCurrency != null ? !feeCurrency.equals(that.feeCurrency) : that.feeCurrency != null) return false;
        if (feeProduct != null ? !feeProduct.equals(that.feeProduct) : that.feeProduct != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) (orderId ^ (orderId >>> 32));
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (feeTb != null ? feeTb.hashCode() : 0);
        result = 31 * result + (feeCurrency != null ? feeCurrency.hashCode() : 0);
        result = 31 * result + (feeProduct != null ? feeProduct.hashCode() : 0);
        return result;
    }
}
