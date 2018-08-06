package com.btctaxi.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * User: guangtou
 * Date: 2018/6/27 15:10
 */
public class AccountingCurrencyPK implements Serializable {
    private String chainName;
    private String currencyName;

    @Column(name = "chain_name", nullable = false, length = 16)
    @Id
    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    @Column(name = "currency_name", nullable = false, length = 16)
    @Id
    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountingCurrencyPK that = (AccountingCurrencyPK) o;

        if (chainName != null ? !chainName.equals(that.chainName) : that.chainName != null) return false;
        if (currencyName != null ? !currencyName.equals(that.currencyName) : that.currencyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chainName != null ? chainName.hashCode() : 0;
        result = 31 * result + (currencyName != null ? currencyName.hashCode() : 0);
        return result;
    }
}
