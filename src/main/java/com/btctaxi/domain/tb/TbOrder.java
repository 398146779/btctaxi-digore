package com.btctaxi.domain.tb;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * User: guangtou
 * Date: 2018/7/9 20:10
 */
@Entity
@Table(name = "tb_order")
@IdClass(TbOrderPK.class)
public class TbOrder {
    private long id;
    private String pairName;
    private long userId;
    private byte direction;
    private BigDecimal price;
    private BigDecimal amount;
    private byte postOnly;
    private Timestamp createTime;
    private Timestamp removeTime;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "pair_name")
    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    @Id
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "direction")
    public byte getDirection() {
        return direction;
    }

    public void setDirection(byte direction) {
        this.direction = direction;
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

    @Basic
    @Column(name = "post_only")
    public byte getPostOnly() {
        return postOnly;
    }

    public void setPostOnly(byte postOnly) {
        this.postOnly = postOnly;
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
    @Column(name = "remove_time")
    public Timestamp getRemoveTime() {
        return removeTime;
    }

    public void setRemoveTime(Timestamp removeTime) {
        this.removeTime = removeTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbOrder tbOrder = (TbOrder) o;

        if (id != tbOrder.id) return false;
        if (userId != tbOrder.userId) return false;
        if (direction != tbOrder.direction) return false;
        if (postOnly != tbOrder.postOnly) return false;
        if (pairName != null ? !pairName.equals(tbOrder.pairName) : tbOrder.pairName != null) return false;
        if (price != null ? !price.equals(tbOrder.price) : tbOrder.price != null) return false;
        if (amount != null ? !amount.equals(tbOrder.amount) : tbOrder.amount != null) return false;
        if (createTime != null ? !createTime.equals(tbOrder.createTime) : tbOrder.createTime != null) return false;
        if (removeTime != null ? !removeTime.equals(tbOrder.removeTime) : tbOrder.removeTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (pairName != null ? pairName.hashCode() : 0);
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) direction;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (int) postOnly;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (removeTime != null ? removeTime.hashCode() : 0);
        return result;
    }
}
