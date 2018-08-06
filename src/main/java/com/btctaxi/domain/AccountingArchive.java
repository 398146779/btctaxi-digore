package com.btctaxi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * User: guangtou
 * Date: 2018/6/27 14:39
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounting_archive")
public class AccountingArchive {
    private Long id;
    private Integer state;
    private String walletTxId;
    private String chainName;
    private String currencyName;
    private String fromAddress;
    private String toAddress;
    private String changeAddress;
    private String memo;
    private BigDecimal amount;
    private BigDecimal fee;
    private String txid;
    private Integer confirm;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Long reviewer1Id;
    private Long reviewer1Nonce;
    private String reviewer1Signature;
    private Timestamp review1Time;
    private String rawTx;
    private Long reviewer2Id;
    private Long reviewer2Nonce;
    private String reviewer2Signature;
    private Timestamp review2Time;
    private String failReason;

    @Basic
    @Column(name = "tx_type")
    private Integer txType;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "state")
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Basic
    @Column(name = "wallet_tx_id")
    public String getWalletTxId() {
        return walletTxId;
    }

    public void setWalletTxId(String walletTxId) {
        this.walletTxId = walletTxId;
    }

    @Basic
    @Column(name = "chain_name")
    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    @Basic
    @Column(name = "currency_name")
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Basic
    @Column(name = "from_address")
    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Basic
    @Column(name = "to_address")
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Basic
    @Column(name = "change_address")
    public String getChangeAddress() {
        return changeAddress;
    }

    public void setChangeAddress(String changeAddress) {
        this.changeAddress = changeAddress;
    }

    @Basic
    @Column(name = "memo")
    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Basic
    @Column(name = "amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Basic
    @Column(name = "fee")
    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @Basic
    @Column(name = "txid")
    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    @Basic
    @Column(name = "confirm")
    public Integer getConfirm() {
        return confirm;
    }

    public void setConfirm(Integer confirm) {
        this.confirm = confirm;
    }

    @Basic
    @Column(name = "create_time")
    @CreationTimestamp
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "update_time")
    @UpdateTimestamp
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "reviewer1_id")
    public Long getReviewer1Id() {
        return reviewer1Id;
    }

    public void setReviewer1Id(Long reviewer1Id) {
        this.reviewer1Id = reviewer1Id;
    }

    @Basic
    @Column(name = "reviewer1_nonce")
    public Long getReviewer1Nonce() {
        return reviewer1Nonce;
    }

    public void setReviewer1Nonce(Long reviewer1Nonce) {
        this.reviewer1Nonce = reviewer1Nonce;
    }

    @Basic
    @Column(name = "reviewer1_signature")
    public String getReviewer1Signature() {
        return reviewer1Signature;
    }

    public void setReviewer1Signature(String reviewer1Signature) {
        this.reviewer1Signature = reviewer1Signature;
    }

    @Basic
    @Column(name = "review1_time")
    public Timestamp getReview1Time() {
        return review1Time;
    }

    public void setReview1Time(Timestamp review1Time) {
        this.review1Time = review1Time;
    }

    @Basic
    @Column(name = "raw_tx")
    public String getRawTx() {
        return rawTx;
    }

    public void setRawTx(String rawTx) {
        this.rawTx = rawTx;
    }

    @Basic
    @Column(name = "reviewer2_id")
    public Long getReviewer2Id() {
        return reviewer2Id;
    }

    public void setReviewer2Id(Long reviewer2Id) {
        this.reviewer2Id = reviewer2Id;
    }

    @Basic
    @Column(name = "reviewer2_nonce")
    public Long getReviewer2Nonce() {
        return reviewer2Nonce;
    }

    public void setReviewer2Nonce(Long reviewer2Nonce) {
        this.reviewer2Nonce = reviewer2Nonce;
    }

    @Basic
    @Column(name = "reviewer2_signature")
    public String getReviewer2Signature() {
        return reviewer2Signature;
    }

    public void setReviewer2Signature(String reviewer2Signature) {
        this.reviewer2Signature = reviewer2Signature;
    }

    @Basic
    @Column(name = "review2_time")
    public Timestamp getReview2Time() {
        return review2Time;
    }

    public void setReview2Time(Timestamp review2Time) {
        this.review2Time = review2Time;
    }

    @Basic
    @Column(name = "fail_reason")
    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountingArchive that = (AccountingArchive) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (walletTxId != null ? !walletTxId.equals(that.walletTxId) : that.walletTxId != null) return false;
        if (chainName != null ? !chainName.equals(that.chainName) : that.chainName != null) return false;
        if (currencyName != null ? !currencyName.equals(that.currencyName) : that.currencyName != null) return false;
        if (fromAddress != null ? !fromAddress.equals(that.fromAddress) : that.fromAddress != null) return false;
        if (toAddress != null ? !toAddress.equals(that.toAddress) : that.toAddress != null) return false;
        if (changeAddress != null ? !changeAddress.equals(that.changeAddress) : that.changeAddress != null)
            return false;
        if (memo != null ? !memo.equals(that.memo) : that.memo != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (fee != null ? !fee.equals(that.fee) : that.fee != null) return false;
        if (txid != null ? !txid.equals(that.txid) : that.txid != null) return false;
        if (confirm != null ? !confirm.equals(that.confirm) : that.confirm != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) return false;
        if (reviewer1Id != null ? !reviewer1Id.equals(that.reviewer1Id) : that.reviewer1Id != null) return false;
        if (reviewer1Nonce != null ? !reviewer1Nonce.equals(that.reviewer1Nonce) : that.reviewer1Nonce != null)
            return false;
        if (reviewer1Signature != null ? !reviewer1Signature.equals(that.reviewer1Signature) : that.reviewer1Signature != null)
            return false;
        if (review1Time != null ? !review1Time.equals(that.review1Time) : that.review1Time != null) return false;
        if (rawTx != null ? !rawTx.equals(that.rawTx) : that.rawTx != null) return false;
        if (reviewer2Id != null ? !reviewer2Id.equals(that.reviewer2Id) : that.reviewer2Id != null) return false;
        if (reviewer2Nonce != null ? !reviewer2Nonce.equals(that.reviewer2Nonce) : that.reviewer2Nonce != null)
            return false;
        if (reviewer2Signature != null ? !reviewer2Signature.equals(that.reviewer2Signature) : that.reviewer2Signature != null)
            return false;
        if (review2Time != null ? !review2Time.equals(that.review2Time) : that.review2Time != null) return false;
        if (failReason != null ? !failReason.equals(that.failReason) : that.failReason != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (walletTxId != null ? walletTxId.hashCode() : 0);
        result = 31 * result + (chainName != null ? chainName.hashCode() : 0);
        result = 31 * result + (currencyName != null ? currencyName.hashCode() : 0);
        result = 31 * result + (fromAddress != null ? fromAddress.hashCode() : 0);
        result = 31 * result + (toAddress != null ? toAddress.hashCode() : 0);
        result = 31 * result + (changeAddress != null ? changeAddress.hashCode() : 0);
        result = 31 * result + (memo != null ? memo.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (fee != null ? fee.hashCode() : 0);
        result = 31 * result + (txid != null ? txid.hashCode() : 0);
        result = 31 * result + (confirm != null ? confirm.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (reviewer1Id != null ? reviewer1Id.hashCode() : 0);
        result = 31 * result + (reviewer1Nonce != null ? reviewer1Nonce.hashCode() : 0);
        result = 31 * result + (reviewer1Signature != null ? reviewer1Signature.hashCode() : 0);
        result = 31 * result + (review1Time != null ? review1Time.hashCode() : 0);
        result = 31 * result + (rawTx != null ? rawTx.hashCode() : 0);
        result = 31 * result + (reviewer2Id != null ? reviewer2Id.hashCode() : 0);
        result = 31 * result + (reviewer2Nonce != null ? reviewer2Nonce.hashCode() : 0);
        result = 31 * result + (reviewer2Signature != null ? reviewer2Signature.hashCode() : 0);
        result = 31 * result + (review2Time != null ? review2Time.hashCode() : 0);
        result = 31 * result + (failReason != null ? failReason.hashCode() : 0);
        return result;
    }
}
