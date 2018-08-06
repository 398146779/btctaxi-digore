package com.btctaxi.mybatis.dao;

import com.btctaxi.mybatis.domain.TbRefundCommission;

public interface TbRefundCommissionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TbRefundCommission record);

    int insertSelective(TbRefundCommission record);

    TbRefundCommission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TbRefundCommission record);

    int updateByPrimaryKey(TbRefundCommission record);
}