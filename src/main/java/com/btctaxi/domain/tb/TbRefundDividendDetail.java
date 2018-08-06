package com.btctaxi.domain.tb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;


/**
 * User: guangtou
 * Date: 2018/7/20 20:21
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
@Table(name = "tb_refund_dividend_detail")
public class TbRefundDividendDetail {
    private long id;
    private long uid;
    private BigDecimal dividendAmount;
    private String dividendUnit;
    private Timestamp createTime;
    private Date dataTime;
    private Integer state;

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
    @Column(name = "uid")
    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
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
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
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

    @Basic
    @Column(name = "state")
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbRefundDividendDetail that = (TbRefundDividendDetail) o;

        if (id != that.id) return false;
        if (uid != that.uid) return false;
        if (dividendAmount != null ? !dividendAmount.equals(that.dividendAmount) : that.dividendAmount != null)
            return false;
        if (dividendUnit != null ? !dividendUnit.equals(that.dividendUnit) : that.dividendUnit != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (dataTime != null ? !dataTime.equals(that.dataTime) : that.dataTime != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (uid ^ (uid >>> 32));
        result = 31 * result + (dividendAmount != null ? dividendAmount.hashCode() : 0);
        result = 31 * result + (dividendUnit != null ? dividendUnit.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (dataTime != null ? dataTime.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }
}
