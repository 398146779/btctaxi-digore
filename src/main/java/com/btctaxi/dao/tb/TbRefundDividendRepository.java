package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbRefundDividend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbRefundDividendRepository extends JpaRepository<TbRefundDividend, Long> {

    List<TbRefundDividend> findAllByDataTimeGreaterThanEqualAndDataTimeLessThanAndState(Date startDate, Date endDate, Integer state);

    TbRefundDividend findByUidAndDataTimeGreaterThanEqualAndDataTimeLessThan(Long userId, Date startDate, Date endDate);
}


