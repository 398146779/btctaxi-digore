package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbRefundCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbRefundCommissionRepository extends JpaRepository<TbRefundCommission, Long> {


//    //汇总 邀请 ID | 已经获取佣金 | 已获取返利
//    @Query("select sum (re.refundProfitAmount) as  refundProfitAmount, sum(re.refundCommissionAmount) as refundCommissionAmount ,sum(re.mineAmount) as mineAmount, re.subUserId, re.subEmail" +
//            " from TbRefundCommission re where re.invitorId = ?1" +
//            " group by re.subUserId,re.subEmail")
//    TbRefundCommission findSummary(Long userId);
//
//
//    //最近返佣 list : 佣金|邮箱|时间
//    @Query("select sum (re.refundProfitAmount) as  refundProfitAmount, sum(re.refundCommissionAmount) as refundCommissionAmount ,sum(re.mineAmount) as mineAmount," +
//            " re.subUserId, re.subEmail, re.createTime " +
//            " from TbRefundCommission re where re.invitorId = :invitorId group by re.subUserId, re.subEmail, re.createTime")
//    Page<TbRefundCommission> listRefundCommission(@Param("invitorId") Long invitorId, Pageable page);
//
//    //最近的反利 list : 佣金|邮箱|时间
//    @Query("select sum (re.refundProfitAmount) as  refundProfitAmount, sum(re.refundCommissionAmount) as refundCommissionAmount ,sum(re.mineAmount) as mineAmount, re.createTime , re.commissionUnit, re.mineUnit" +
//            " from TbRefundCommission re where re.subUserId = :subUserId group by re.subUserId, re.subEmail, re.createTime")
//    Page<TbRefundCommission> listRefundProfit(@Param("subUserId") Long subUserId, Pageable page);
//
//
//    //排名 : 邮箱|佣金
//    @Query("select   sum(re.refundCommissionAmount) as refundCommissionAmount  , re.subEmail  , re.commissionUnit, re.mineUnit " +
//            " from TbRefundCommission re " +
//            " group by re.subEmail" +
//            " order by refundCommissionAmount desc")
//    List<Object[]> leaderBoard();





}


