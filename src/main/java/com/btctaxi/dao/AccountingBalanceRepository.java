package com.btctaxi.dao;

import genesis.accounting.domain.AccountingBalance;
import genesis.accounting.domain.AccountingBalancePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface AccountingBalanceRepository extends JpaRepository<AccountingBalance, AccountingBalancePK> {

    AccountingBalance findByUserIdAndCurrencyName(Long userId, String currencyName);

    List<AccountingBalance> findAllByAvailableGreaterThanAndCurrencyName(BigDecimal available, String currencyName);

    List<AccountingBalance> findAllByCurrencyName(String currency);

    List<AccountingBalance> findAllByUserIdAndAvailableGreaterThan(Long userId, BigDecimal available);

    List<AccountingBalance> findAllByUserIdInAndAvailableGreaterThan(List<Long> userId, BigDecimal available);


    List<AccountingBalance> findAllByUserId(Long userId);





}


