package com.btctaxi.domain.tb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: guangtou
 * Date: 2018/7/9 20:17
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_refund_commission")
public class TbRefundCommission {
    private long id;
    private long subUserId;
    private String subEmail;
    private Long invitorId;
    private String invitorEmail;
    private String pairName;
    private BigDecimal feeTb;
    private BigDecimal feeCurrency;
    private BigDecimal feeProduct;
    private BigDecimal refundCommissionAmount;
    private String refundCommissionUnit;
    private BigDecimal refundLockAmount;
    private String refundLockUnit;
    private BigDecimal refundProfitAmount;
    private String refundProfitUnit;
    private BigDecimal dividendAmount;
    private String dividendUnit;
    private BigDecimal mineAmount;
    private String mineUnit;
    private Date createTime;
    private int hour;
    private long orderId;
    @Basic @Column(name = "price")
    private BigDecimal price;
    @Basic @Column(name = "amount")
    private BigDecimal amount;
    @Basic @Column(name = "direction")
    private byte direction;
    @Basic @Column(name = "commission_btc_amount")
    private BigDecimal commissionBtcAmount;



    @Id
    @Column(name = "id")
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "sub_user_id")
    public long getSubUserId() {
        return subUserId;
    }

    public void setSubUserId(long subUserId) {
        this.subUserId = subUserId;
    }

    @Basic
    @Column(name = "sub_email")
    public String getSubEmail() {
        return subEmail;
    }

    public void setSubEmail(String subEmail) {
        this.subEmail = subEmail;
    }

    @Basic
    @Column(name = "invitor_id")
    public Long getInvitorId() {
        return invitorId;
    }

    public void setInvitorId(Long invitorId) {
        this.invitorId = invitorId;
    }

    @Basic
    @Column(name = "invitor_email")
    public String getInvitorEmail() {
        return invitorEmail;
    }

    public void setInvitorEmail(String invitorEmail) {
        this.invitorEmail = invitorEmail;
    }

    @Basic
    @Column(name = "pair_name")
    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
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

    @Basic
    @Column(name = "refund_commission_amount")
    public BigDecimal getRefundCommissionAmount() {
        return refundCommissionAmount;
    }

    public void setRefundCommissionAmount(BigDecimal refundCommissionAmount) {
        this.refundCommissionAmount = refundCommissionAmount;
    }

    @Basic
    @Column(name = "refund_commission_unit")
    public String getRefundCommissionUnit() {
        return refundCommissionUnit;
    }

    public void setRefundCommissionUnit(String refundCommissionUnit) {
        this.refundCommissionUnit = refundCommissionUnit;
    }

    @Basic
    @Column(name = "refund_lock_amount")
    public BigDecimal getRefundLockAmount() {
        return refundLockAmount;
    }

    public void setRefundLockAmount(BigDecimal refundLockAmount) {
        this.refundLockAmount = refundLockAmount;
    }

    @Basic
    @Column(name = "refund_lock_unit")
    public String getRefundLockUnit() {
        return refundLockUnit;
    }

    public void setRefundLockUnit(String refundLockUnit) {
        this.refundLockUnit = refundLockUnit;
    }

    @Basic
    @Column(name = "refund_profit_amount")
    public BigDecimal getRefundProfitAmount() {
        return refundProfitAmount;
    }

    public void setRefundProfitAmount(BigDecimal refundProfitAmount) {
        this.refundProfitAmount = refundProfitAmount;
    }

    @Basic
    @Column(name = "refund_profit_unit")
    public String getRefundProfitUnit() {
        return refundProfitUnit;
    }

    public void setRefundProfitUnit(String refundProfitUnit) {
        this.refundProfitUnit = refundProfitUnit;
    }

    @Basic
    @Column(name = "dividend_amount")
    public BigDecimal getDividendAmount() {
        return dividendAmount;
    }

    public void setDividendAmount(BigDecimal dividendAmount) {
        this.dividendAmount = dividendAmount;
    }

    @Basic
    @Column(name = "dividend_unit")
    public String getDividendUnit() {
        return dividendUnit;
    }

    public void setDividendUnit(String dividendUnit) {
        this.dividendUnit = dividendUnit;
    }

    @Basic
    @Column(name = "mine_amount")
    public BigDecimal getMineAmount() {
        return mineAmount;
    }

    public void setMineAmount(BigDecimal mineAmount) {
        this.mineAmount = mineAmount;
    }

    @Basic
    @Column(name = "mine_unit")
    public String getMineUnit() {
        return mineUnit;
    }

    public void setMineUnit(String mineUnit) {
        this.mineUnit = mineUnit;
    }

    @Basic
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "hour")
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @Basic
    @Column(name = "order_id")
    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbRefundCommission that = (TbRefundCommission) o;

        if (id != that.id) return false;
        if (subUserId != that.subUserId) return false;
        if (hour != that.hour) return false;
        if (orderId != that.orderId) return false;
        if (subEmail != null ? !subEmail.equals(that.subEmail) : that.subEmail != null) return false;
        if (invitorId != null ? !invitorId.equals(that.invitorId) : that.invitorId != null) return false;
        if (invitorEmail != null ? !invitorEmail.equals(that.invitorEmail) : that.invitorEmail != null) return false;
        if (pairName != null ? !pairName.equals(that.pairName) : that.pairName != null) return false;
        if (feeTb != null ? !feeTb.equals(that.feeTb) : that.feeTb != null) return false;
        if (feeCurrency != null ? !feeCurrency.equals(that.feeCurrency) : that.feeCurrency != null) return false;
        if (feeProduct != null ? !feeProduct.equals(that.feeProduct) : that.feeProduct != null) return false;
        if (refundCommissionAmount != null ? !refundCommissionAmount.equals(that.refundCommissionAmount) : that.refundCommissionAmount != null)
            return false;
        if (refundCommissionUnit != null ? !refundCommissionUnit.equals(that.refundCommissionUnit) : that.refundCommissionUnit != null)
            return false;
        if (refundLockAmount != null ? !refundLockAmount.equals(that.refundLockAmount) : that.refundLockAmount != null)
            return false;
        if (refundLockUnit != null ? !refundLockUnit.equals(that.refundLockUnit) : that.refundLockUnit != null)
            return false;
        if (refundProfitAmount != null ? !refundProfitAmount.equals(that.refundProfitAmount) : that.refundProfitAmount != null)
            return false;
        if (refundProfitUnit != null ? !refundProfitUnit.equals(that.refundProfitUnit) : that.refundProfitUnit != null)
            return false;
        if (dividendAmount != null ? !dividendAmount.equals(that.dividendAmount) : that.dividendAmount != null)
            return false;
        if (dividendUnit != null ? !dividendUnit.equals(that.dividendUnit) : that.dividendUnit != null) return false;
        if (mineAmount != null ? !mineAmount.equals(that.mineAmount) : that.mineAmount != null) return false;
        if (mineUnit != null ? !mineUnit.equals(that.mineUnit) : that.mineUnit != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (subUserId ^ (subUserId >>> 32));
        result = 31 * result + (subEmail != null ? subEmail.hashCode() : 0);
        result = 31 * result + (invitorId != null ? invitorId.hashCode() : 0);
        result = 31 * result + (invitorEmail != null ? invitorEmail.hashCode() : 0);
        result = 31 * result + (pairName != null ? pairName.hashCode() : 0);
        result = 31 * result + (feeTb != null ? feeTb.hashCode() : 0);
        result = 31 * result + (feeCurrency != null ? feeCurrency.hashCode() : 0);
        result = 31 * result + (feeProduct != null ? feeProduct.hashCode() : 0);
        result = 31 * result + (refundCommissionAmount != null ? refundCommissionAmount.hashCode() : 0);
        result = 31 * result + (refundCommissionUnit != null ? refundCommissionUnit.hashCode() : 0);
        result = 31 * result + (refundLockAmount != null ? refundLockAmount.hashCode() : 0);
        result = 31 * result + (refundLockUnit != null ? refundLockUnit.hashCode() : 0);
        result = 31 * result + (refundProfitAmount != null ? refundProfitAmount.hashCode() : 0);
        result = 31 * result + (refundProfitUnit != null ? refundProfitUnit.hashCode() : 0);
        result = 31 * result + (dividendAmount != null ? dividendAmount.hashCode() : 0);
        result = 31 * result + (dividendUnit != null ? dividendUnit.hashCode() : 0);
        result = 31 * result + (mineAmount != null ? mineAmount.hashCode() : 0);
        result = 31 * result + (mineUnit != null ? mineUnit.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + hour;
        result = 31 * result + (int) (orderId ^ (orderId >>> 32));
        return result;
    }
}