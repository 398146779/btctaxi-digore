package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbRefundDividendDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbRefundDividendDetailRepository extends JpaRepository<TbRefundDividendDetail, Long> {

    List<TbRefundDividendDetail> findAllByDataTimeGreaterThanEqualAndDataTimeLessThanAndState(Date startDate, Date endDate, Integer state);


}


