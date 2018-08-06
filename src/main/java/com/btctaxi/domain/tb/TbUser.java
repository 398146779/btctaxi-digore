package com.btctaxi.domain.tb;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * User: guangtou
 * Date: 2018/7/5 17:35
 */
@Entity
@Table(name = "tb_user" )
public class TbUser {
    private long id;
    private String email;
    private String pass;
    private String salt;
    private String nick;
    private Long invitorId;
    private String googleKey;
    private Integer regionId;
    private String phone;
    private String locale;
    private Timestamp freezeTime;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    @Column(name = "pass")
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Basic
    @Column(name = "salt")
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Basic
    @Column(name = "nick")
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
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
    @Column(name = "google_key")
    public String getGoogleKey() {
        return googleKey;
    }

    public void setGoogleKey(String googleKey) {
        this.googleKey = googleKey;
    }

    @Basic
    @Column(name = "region_id")
    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    @Basic
    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "locale")
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Basic
    @Column(name = "freeze_time")
    public Timestamp getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Timestamp freezeTime) {
        this.freezeTime = freezeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TbUser tbUser = (TbUser) o;

        if (id != tbUser.id) return false;
        if (email != null ? !email.equals(tbUser.email) : tbUser.email != null) return false;
        if (pass != null ? !pass.equals(tbUser.pass) : tbUser.pass != null) return false;
        if (salt != null ? !salt.equals(tbUser.salt) : tbUser.salt != null) return false;
        if (nick != null ? !nick.equals(tbUser.nick) : tbUser.nick != null) return false;
        if (invitorId != null ? !invitorId.equals(tbUser.invitorId) : tbUser.invitorId != null) return false;
        if (googleKey != null ? !googleKey.equals(tbUser.googleKey) : tbUser.googleKey != null) return false;
        if (regionId != null ? !regionId.equals(tbUser.regionId) : tbUser.regionId != null) return false;
        if (phone != null ? !phone.equals(tbUser.phone) : tbUser.phone != null) return false;
        if (locale != null ? !locale.equals(tbUser.locale) : tbUser.locale != null) return false;
        if (freezeTime != null ? !freezeTime.equals(tbUser.freezeTime) : tbUser.freezeTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (pass != null ? pass.hashCode() : 0);
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        result = 31 * result + (nick != null ? nick.hashCode() : 0);
        result = 31 * result + (invitorId != null ? invitorId.hashCode() : 0);
        result = 31 * result + (googleKey != null ? googleKey.hashCode() : 0);
        result = 31 * result + (regionId != null ? regionId.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (freezeTime != null ? freezeTime.hashCode() : 0);
        return result;
    }
}
