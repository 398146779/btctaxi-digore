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
 * Date: 2018/7/14 20:27
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_refund_dividend")
public class TbRefundDividend {


    private Long id;
    private Long uid;
    private String email;
    private Long inviterId;
    private String inviterEmail;
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
    private Date dataTime;
    @Basic
    @Column(name = "state")
    private Integer state;

    @Id
    @Column(name = "id")
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "uid")
    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "inviter_id")
    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    @Basic
    @Column(name = "inviter_email")
    public String getInviterEmail() {
        return inviterEmail;
    }

    public void setInviterEmail(String inviterEmail) {
        this.inviterEmail = inviterEmail;
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
    @Column(name = "data_time")
    public Date getDataTime() {
        return dataTime;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbRefundDividend that = (TbRefundDividend) o;

        if (id != that.id) return false;
        if (uid != that.uid) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (inviterId != null ? !inviterId.equals(that.inviterId) : that.inviterId != null) return false;
        if (inviterEmail != null ? !inviterEmail.equals(that.inviterEmail) : that.inviterEmail != null) return false;
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
        if (dataTime != null ? !dataTime.equals(that.dataTime) : that.dataTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (uid ^ (uid >>> 32));
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (inviterId != null ? inviterId.hashCode() : 0);
        result = 31 * result + (inviterEmail != null ? inviterEmail.hashCode() : 0);
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
        result = 31 * result + (dataTime != null ? dataTime.hashCode() : 0);
        return result;
    }
}
