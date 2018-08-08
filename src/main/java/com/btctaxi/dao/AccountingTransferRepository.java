package com.btctaxi.dao;

import com.btctaxi.domain.AccountingTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface AccountingTransferRepository extends JpaRepository<AccountingTransfer, Long> {

    Page<AccountingTransfer> findALLByFromUserId(Long fromUserId, Pageable pageable);

    Page<AccountingTransfer> findALLByToUserId(Long toUserId, Pageable pageable);

    Page<AccountingTransfer> findALLByFromUserIdAndCurrencyName(Long fromUserId, String currencyName, Pageable pageable);

    Page<AccountingTransfer> findALLByToUserIdAndCurrencyName(Long toUserId, String currencyName, Pageable pageable);


}


