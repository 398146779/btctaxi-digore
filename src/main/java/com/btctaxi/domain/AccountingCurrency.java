package com.btctaxi.domain;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * User: guangtou
 * Date: 2018/6/27 15:10
 */
@Entity
@Table(name = "accounting_currency")
@IdClass(AccountingCurrencyPK.class)
@Data
public class AccountingCurrency {
    private String chainName;
    private String currencyName;
    private String fullName;
    private byte scale;
    private int exchangeConfirm;
    private int withdrawConfirm;
    private BigDecimal minDepositAmount;
    private BigDecimal minWithdrawAmount;
    private BigDecimal minReviewAmount;
    private BigDecimal withdrawFee;
    @Basic
    @Column(name = "miner_min_amount", nullable = false, length = 18)
    private BigDecimal minerMinAmount;
    @Basic
    @Column(name = "miner_amount", nullable = false, precision = 18)
    private BigDecimal minerAmount;
    @Basic
    @Column(name = "archive_amount", nullable = false, precision = 18)
    private BigDecimal archiveAmount;

    private byte depositable;
    private byte withdrawable;
    private byte memoSupport;
    private String scanUrl;


    @Id
    @Column(name = "chain_name", nullable = false, length = 16)
    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    @Id
    @Column(name = "currency_name", nullable = false, length = 16)
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Basic
    @Column(name = "full_name", nullable = false, length = 64)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Basic
    @Column(name = "scale", nullable = false)
    public byte getScale() {
        return scale;
    }

    public void setScale(byte scale) {
        this.scale = scale;
    }

    @Basic
    @Column(name = "exchange_confirm", nullable = false)
    public int getExchangeConfirm() {
        return exchangeConfirm;
    }

    public void setExchangeConfirm(int exchangeConfirm) {
        this.exchangeConfirm = exchangeConfirm;
    }

    @Basic
    @Column(name = "withdraw_confirm", nullable = false)
    public int getWithdrawConfirm() {
        return withdrawConfirm;
    }

    public void setWithdrawConfirm(int withdrawConfirm) {
        this.withdrawConfirm = withdrawConfirm;
    }

    @Basic
    @Column(name = "min_deposit_amount", nullable = false, precision = 18)
    public BigDecimal getMinDepositAmount() {
        return minDepositAmount;
    }

    public void setMinDepositAmount(BigDecimal minDepositAmount) {
        this.minDepositAmount = minDepositAmount;
    }

    @Basic
    @Column(name = "min_withdraw_amount", nullable = false, precision = 18)
    public BigDecimal getMinWithdrawAmount() {
        return minWithdrawAmount;
    }

    public void setMinWithdrawAmount(BigDecimal minWithdrawAmount) {
        this.minWithdrawAmount = minWithdrawAmount;
    }

    @Basic
    @Column(name = "min_review_amount", nullable = false, precision = 18)
    public BigDecimal getMinReviewAmount() {
        return minReviewAmount;
    }

    public void setMinReviewAmount(BigDecimal minReviewAmount) {
        this.minReviewAmount = minReviewAmount;
    }

    @Basic
    @Column(name = "withdraw_fee", nullable = false, precision = 18)
    public BigDecimal getWithdrawFee() {
        return withdrawFee;
    }

    public void setWithdrawFee(BigDecimal withdrawFee) {
        this.withdrawFee = withdrawFee;
    }

    @Basic
    @Column(name = "depositable", nullable = false)
    public byte getDepositable() {
        return depositable;
    }

    public void setDepositable(byte depositable) {
        this.depositable = depositable;
    }

    @Basic
    @Column(name = "withdrawable", nullable = false)
    public byte getWithdrawable() {
        return withdrawable;
    }

    public void setWithdrawable(byte withdrawable) {
        this.withdrawable = withdrawable;
    }

    @Basic
    @Column(name = "memo_support", nullable = false)
    public byte getMemoSupport() {
        return memoSupport;
    }

    public void setMemoSupport(byte memoSupport) {
        this.memoSupport = memoSupport;
    }

    @Basic
    @Column(name = "scan_url", nullable = false, length = 256)
    public String getScanUrl() {
        return scanUrl;
    }

    public void setScanUrl(String scanUrl) {
        this.scanUrl = scanUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountingCurrency that = (AccountingCurrency) o;

        if (scale != that.scale) return false;
        if (exchangeConfirm != that.exchangeConfirm) return false;
        if (withdrawConfirm != that.withdrawConfirm) return false;
        if (depositable != that.depositable) return false;
        if (withdrawable != that.withdrawable) return false;
        if (memoSupport != that.memoSupport) return false;
        if (chainName != null ? !chainName.equals(that.chainName) : that.chainName != null) return false;
        if (currencyName != null ? !currencyName.equals(that.currencyName) : that.currencyName != null) return false;
        if (fullName != null ? !fullName.equals(that.fullName) : that.fullName != null) return false;
        if (minDepositAmount != null ? !minDepositAmount.equals(that.minDepositAmount) : that.minDepositAmount != null)
            return false;
        if (minWithdrawAmount != null ? !minWithdrawAmount.equals(that.minWithdrawAmount) : that.minWithdrawAmount != null)
            return false;
        if (minReviewAmount != null ? !minReviewAmount.equals(that.minReviewAmount) : that.minReviewAmount != null)
            return false;
        if (withdrawFee != null ? !withdrawFee.equals(that.withdrawFee) : that.withdrawFee != null) return false;
        if (scanUrl != null ? !scanUrl.equals(that.scanUrl) : that.scanUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chainName != null ? chainName.hashCode() : 0;
        result = 31 * result + (currencyName != null ? currencyName.hashCode() : 0);
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (int) scale;
        result = 31 * result + exchangeConfirm;
        result = 31 * result + withdrawConfirm;
        result = 31 * result + (minDepositAmount != null ? minDepositAmount.hashCode() : 0);
        result = 31 * result + (minWithdrawAmount != null ? minWithdrawAmount.hashCode() : 0);
        result = 31 * result + (minReviewAmount != null ? minReviewAmount.hashCode() : 0);
        result = 31 * result + (withdrawFee != null ? withdrawFee.hashCode() : 0);
        result = 31 * result + (int) depositable;
        result = 31 * result + (int) withdrawable;
        result = 31 * result + (int) memoSupport;
        result = 31 * result + (scanUrl != null ? scanUrl.hashCode() : 0);
        return result;
    }
}
