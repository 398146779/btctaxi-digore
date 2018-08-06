package com.btctaxi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * User: guangtou
 * Date: 2018/7/3 20:20
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "accounting_balance" )
@IdClass(AccountingBalancePK.class)
public class AccountingBalance {
    private Long userId;
    private String currencyName;
    private BigDecimal available;
    private BigDecimal ordering;
    private BigDecimal withdrawing;
    private BigDecimal locking;

    @Id
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Id
    @Column(name = "currency_name")
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Basic
    @Column(name = "available")
    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    @Basic
    @Column(name = "ordering")
    public BigDecimal getOrdering() {
        return ordering;
    }

    public void setOrdering(BigDecimal ordering) {
        this.ordering = ordering;
    }

    @Basic
    @Column(name = "withdrawing")
    public BigDecimal getWithdrawing() {
        return withdrawing;
    }

    public void setWithdrawing(BigDecimal withdrawing) {
        this.withdrawing = withdrawing;
    }

    @Basic
    @Column(name = "locking")
    public BigDecimal getLocking() {
        return locking;
    }

    public void setLocking(BigDecimal locking) {
        this.locking = locking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountingBalance that = (AccountingBalance) o;

        if (userId != that.userId) return false;
        if (currencyName != null ? !currencyName.equals(that.currencyName) : that.currencyName != null) return false;
        if (available != null ? !available.equals(that.available) : that.available != null) return false;
        if (ordering != null ? !ordering.equals(that.ordering) : that.ordering != null) return false;
        if (withdrawing != null ? !withdrawing.equals(that.withdrawing) : that.withdrawing != null) return false;
        if (locking != null ? !locking.equals(that.locking) : that.locking != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (currencyName != null ? currencyName.hashCode() : 0);
        result = 31 * result + (available != null ? available.hashCode() : 0);
        result = 31 * result + (ordering != null ? ordering.hashCode() : 0);
        result = 31 * result + (withdrawing != null ? withdrawing.hashCode() : 0);
        result = 31 * result + (locking != null ? locking.hashCode() : 0);
        return result;
    }
}
