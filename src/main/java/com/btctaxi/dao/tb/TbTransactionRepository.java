package com.btctaxi.dao.tb;

import com.btctaxi.domain.tb.TbTransaction;
import com.btctaxi.domain.tb.TbTransactionPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbTransactionRepository extends JpaRepository<TbTransaction, TbTransactionPK> {


    List<TbTransaction> findAllByUserIdInAndCreateTimeBetween(List<Long> userIds, Date startDate, Date endDate);

    List<TbTransaction> findAllByUserIdAndCreateTimeBetween(Long userId, Date startDate, Date endDate);

    @Query(value = "select  sum(fee_tb) as fee_tb, sum(fee_currency) as fee_currency, sum(fee_product) as fee_product, user_id, order_id, create_time, sum(price) price , sum(amount) amount" +
            " from tb_transaction " +
            " where create_time >= ?1 AND create_time < ?2  and (fee_tb + fee_currency + fee_product ) > 0 " +
            " group by user_id, order_id, create_time", nativeQuery = true)
    List<Object[]> allTransactionList(Date startDate, Date endDate);


    @Query(value = "select  sum(fee_tb) as fee_tb, sum(fee_currency) as fee_currency, sum(fee_product) as fee_product, user_id, order_id, create_time" +
            " from tb_transaction " +
            " where create_time >= ?1 AND create_time < ?2  and (fee_tb + fee_currency + fee_product ) > 0 " +
            " group by user_id, order_id, create_time", nativeQuery = true)
    List allTbtransactionVoList(Date startDate, Date endDate);


}


