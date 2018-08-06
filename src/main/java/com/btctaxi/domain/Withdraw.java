package com.btctaxi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * _user: guangtou
 * _date: 2018/6/22 15:48
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Withdraw {

    private Long id;

    private  String id_str;

    private Long user_id;

    private  String chain_name;//链名

    private  String currency_name;//币名

    private  String to_address;//目标地址

    private BigDecimal amount;//提币量

    private  BigDecimal fee;//提币费

    private  String txid;//链上的txid

    private  String memo;

    private  Integer confirm;//确认数

    private Timestamp  create_time;

    private Timestamp  update_time;

    private  Integer state;//状态
    //  @_enumerated()
//  private _state state;//状态

//    private Boolean unread;//是否已读

    private int version;

    private Long reviewer1_id;//一审人员_i_d

    private Long reviewer1_nonce;//一审 nonce 由_o_a生成

    private  String reviewer1_signature;//_h_a1with_r_s_a(wallet_tx_id+chain_name+currency_name+from_address+to_address+change_address+amount+reviewer1_id+reviewer1_nonce)

    private Timestamp review1_time;//插入时间二审关注的数据

    private  String raw_tx;//链上转账原始数据

    private Long reviewer2_id;//一审人员_i_d

    private Long reviewer2_nonce;//一审 nonce 由_o_a生成

    private  String reviewer2_signature;//_s_h_a1with_r_s_a(reviewer1_signature+wallet_tx_id+raw_tx+reviewer2_id+reviewer2_nonce)

    private Timestamp  review2_time;//插入时间二审关注的数据

    private  String wallet_tx_id; //'钱包流水_i_d, sha256(key+0+id+currency_name)',

    private  String from_address; //源地址

    private  String change_address; //找零地址, 默认为 frome

    private  String fail_reason; //失败原因

}
