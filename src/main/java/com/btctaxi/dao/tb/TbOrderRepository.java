package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbOrder;
import genesis.accounting.domain.tb.TbOrderPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbOrderRepository extends JpaRepository<TbOrder, TbOrderPK> {


    TbOrder findByUserIdAndIdAndCreateTimeLessThanEqual(Long userId, Long orderId, Date transactionDate);

}


