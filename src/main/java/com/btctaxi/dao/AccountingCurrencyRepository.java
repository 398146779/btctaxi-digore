package com.btctaxi.dao;

import genesis.accounting.domain.AccountingCurrency;
import genesis.accounting.domain.AccountingCurrencyPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface AccountingCurrencyRepository extends JpaRepository<AccountingCurrency, AccountingCurrencyPK> {
    List<AccountingCurrency> findAllByChainName(String chainName);

}
