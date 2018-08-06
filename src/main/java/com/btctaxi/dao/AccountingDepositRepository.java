package com.btctaxi.dao;

import genesis.accounting.domain.AccountingDeposit;
import genesis.accounting.domain.AccountingDepositPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface AccountingDepositRepository extends JpaRepository<AccountingDeposit, AccountingDepositPK> {

    List<AccountingDeposit> findAllByStateAndChainNameAndCurrencyNameAndUpdateTimeAfter(int state, String chainName, String currency, Date time);

    List<AccountingDeposit> findAllByStateAndChainNameAndCreateTimeAfter(byte state, String chainName, Date time);


}
