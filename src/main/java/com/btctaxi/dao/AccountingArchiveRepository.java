package com.btctaxi.dao;

import com.btctaxi.domain.AccountingArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface AccountingArchiveRepository extends JpaRepository<AccountingArchive, Long> {

    @Query("from AccountingArchive where state in :states  and chainName=:chainName and currencyName = :currencyName  and fromAddress = :fromAddress ")
    List<AccountingArchive> findAllPaddingArchiveByFromAddress(@Param("states") List<Integer> states,
                                                               @Param("fromAddress") String fromAddress,
//                                                            @Param("toAddress") String toAddress,
                                                               @Param("chainName") String chainName,
                                                               @Param("currencyName") String currencyName);

    @Query("from AccountingArchive where state in :states  and chainName=:chainName and currencyName = :currencyName  and   toAddress = :toAddress ")
    List<AccountingArchive> findAllPaddingArchiveByToAddress(@Param("states") List<Integer> states,
//                                                            @Param("fromAddress") String fromAddress,
                                                             @Param("toAddress") String toAddress,
                                                             @Param("chainName") String chainName,
                                                             @Param("currencyName") String currencyName);

    Page<AccountingArchive> findAllByStateInOrderByCreateTimeAsc(Pageable pageable, List<Integer> states);

    Page<AccountingArchive> findAllByStateInOrderByTxTypeAscCreateTimeAsc(Pageable pageable, List<Integer> states);

}


