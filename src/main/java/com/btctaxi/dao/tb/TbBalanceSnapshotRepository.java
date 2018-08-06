package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbBalanceSnapshotRepository extends JpaRepository<TbBalanceSnapshot, Long> {




}


