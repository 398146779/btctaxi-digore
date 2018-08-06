package com.btctaxi.gate.service;

import genesis.common.DataMap;
import genesis.gate.error.ServiceError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏
 */
@Service
public class FavoritePairService extends BaseService {
    /**
     * 收藏列表
     *
     * @param userId 用户id
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataMap> list(long userId) {
//        String sql = "SELECT pair_name FROM tb_favorite_pair WHERE uid = ?";
        String sql = "SELECT pair_name FROM tb_favorite_pair a JOIN tb_pair b ON a.pair_name=b.name WHERE a.uid = ? AND b.state = 'ONLINE'";
        return data.query(sql, userId);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void create(long userId, String pairName) {
        String sql = "SELECT name FROM tb_pair WHERE name = ?";
        DataMap pair = data.queryOne(sql, pairName);
        if (pair == null)
            throw new ServiceError(ServiceError.FAVORITE_CREATE_PAIR_NOT_EXISTS);

        sql = "INSERT IGNORE INTO tb_favorite_pair(uid, pair_name) VALUES(?, ?)";
        data.update(sql, userId, pairName);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void remove(long userId, String pairName) {
        String sql = "DELETE FROM tb_favorite_pair WHERE uid = ? AND pair_name = ?";
        data.update(sql, userId, pairName);
    }
}
