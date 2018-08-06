package com.btctaxi.mybatis.domain;

import java.math.BigDecimal;
import java.util.Date;

public class TbRefundCommission {
    private Long id;

    private Long subUserId;

    private String subEmail;

    private Long invitorId;

    private String invitorEmail;

    private String pairName;

    private BigDecimal feeTb;

    private BigDecimal feeCurrency;

    private BigDecimal feeProduct;

    private BigDecimal refundCommissionRate;

    private BigDecimal refundCommissionAmount;

    private BigDecimal refundProfitRate;

    private BigDecimal refundProfitAmount;

    private BigDecimal mineRate;

    private BigDecimal mineAmount;

    private Date createTime;

    private String commissionUnit;

    private String mineUnit;

    private Long orderId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubUserId() {
        return subUserId;
    }

    public void setSubUserId(Long subUserId) {
        this.subUserId = subUserId;
    }

    public String getSubEmail() {
        return subEmail;
    }

    public void setSubEmail(String subEmail) {
        this.subEmail = subEmail == null ? null : subEmail.trim();
    }

    public Long getInvitorId() {
        return invitorId;
    }

    public void setInvitorId(Long invitorId) {
        this.invitorId = invitorId;
    }

    public String getInvitorEmail() {
        return invitorEmail;
    }

    public void setInvitorEmail(String invitorEmail) {
        this.invitorEmail = invitorEmail == null ? null : invitorEmail.trim();
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName == null ? null : pairName.trim();
    }

    public BigDecimal getFeeTb() {
        return feeTb;
    }

    public void setFeeTb(BigDecimal feeTb) {
        this.feeTb = feeTb;
    }

    public BigDecimal getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(BigDecimal feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    public BigDecimal getFeeProduct() {
        return feeProduct;
    }

    public void setFeeProduct(BigDecimal feeProduct) {
        this.feeProduct = feeProduct;
    }

    public BigDecimal getRefundCommissionRate() {
        return refundCommissionRate;
    }

    public void setRefundCommissionRate(BigDecimal refundCommissionRate) {
        this.refundCommissionRate = refundCommissionRate;
    }

    public BigDecimal getRefundCommissionAmount() {
        return refundCommissionAmount;
    }

    public void setRefundCommissionAmount(BigDecimal refundCommissionAmount) {
        this.refundCommissionAmount = refundCommissionAmount;
    }

    public BigDecimal getRefundProfitRate() {
        return refundProfitRate;
    }

    public void setRefundProfitRate(BigDecimal refundProfitRate) {
        this.refundProfitRate = refundProfitRate;
    }

    public BigDecimal getRefundProfitAmount() {
        return refundProfitAmount;
    }

    public void setRefundProfitAmount(BigDecimal refundProfitAmount) {
        this.refundProfitAmount = refundProfitAmount;
    }

    public BigDecimal getMineRate() {
        return mineRate;
    }

    public void setMineRate(BigDecimal mineRate) {
        this.mineRate = mineRate;
    }

    public BigDecimal getMineAmount() {
        return mineAmount;
    }

    public void setMineAmount(BigDecimal mineAmount) {
        this.mineAmount = mineAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCommissionUnit() {
        return commissionUnit;
    }

    public void setCommissionUnit(String commissionUnit) {
        this.commissionUnit = commissionUnit == null ? null : commissionUnit.trim();
    }

    public String getMineUnit() {
        return mineUnit;
    }

    public void setMineUnit(String mineUnit) {
        this.mineUnit = mineUnit == null ? null : mineUnit.trim();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}