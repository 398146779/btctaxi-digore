package com.btctaxi.dao.tb;

import genesis.accounting.domain.tb.TbUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: guangtou
 * Date: 2018/6/27 14:44
 */
@Repository
public interface TbUserRepository extends JpaRepository<TbUser, Long> {

    /**
     * 获取当前用户邀请列表
     *
     * @param userId 当前用户
     */
    Page<TbUser> findAllByInvitorId(Long userId, Pageable pageable);

    List<TbUser> findAllByInvitorId(Long userId);

}


