package com.btctaxi.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genesis.accounting.config.ConstantUtils;
import genesis.accounting.config.WalletConfig;
import genesis.common.Data;
import genesis.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EosRamService {

    private Data data;

    private WalletConfig walletConfig;

    @Autowired
    private HttpService httpService;

    public EosRamService(Data data, WalletConfig walletConfig, HttpService http) {
        this.data = data;
        this.walletConfig = walletConfig;
        this.httpService = http;
    }

    /**
     * 查询EOSRAM交易历史记录
     * @param size
     * @return
     */
    public List<DataMap> getEosRamList(Long user_id, Integer size){
        List<DataMap> pairs = null;
        ArrayList<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" select  eos_number,eosram_transaction_price, create_time,txid,inorout,eosram_number from order_eosram where  txid is not null ");

        if(user_id != null && user_id != 0){
            sql.append(" and user_id = "+user_id);
        }
        if (size>0){
            sql.append(" order by create_time desc limit 0,"+size);
        }
        pairs = data.query(sql.toString());
        return pairs;
    }

    /**
     * 查询EOSRAM交易历史记录
     * @param size
     * @return
     */
    public List<DataMap> getEosRamList(Integer size){
        List<DataMap> pairs = null;
        ArrayList<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" select  eos_number,eosram_transaction_price, create_time,txid,inorout,eosram_number from order_eosram where  txid is not null ");
        sql.append(" order by create_time desc limit 0, ? ");
        pairs = data.query(sql.toString(), size);
        return pairs;
    }

    /**
     * 查询eosram价格
     *
     * @return
     */
    public JSONObject getEosRamPrice() {
        JSONObject post = new JSONObject();
        try {
            post = JSON.parseObject(httpService.get(walletConfig.getUrl5432()+ConstantUtils.GETEOSRAM));
            post.put("key","true");
            post.put("message","操作成功");
        } catch (Exception e) {
            post.put("key","false");
            post.put("message",e.getMessage());
        }
        return post;
    }

    /**
     * 购买eosram
     * @return
     */

    @Transactional(rollbackFor = Throwable.class)
    public JSONObject buyEosRam(Long userId, BigDecimal eos_number){
//        //TODO 查询用户eos金额是否足够，不够提示余额不足，结束
        String sql = "SELECT available from accounting_balance where user_id = ? and currency_name='EOS'";
        DataMap balance = data.queryOne(sql, userId);
        if (balance == null)
            throw new RuntimeException("balace_not_enough");
        //用户eos
        // 账户余额
        BigDecimal eosavailable = balance.getBig("available");
        if (eosavailable.compareTo(BigDecimal.ZERO) == 0)
            throw new RuntimeException("balace_not_enough");
        //TODO 锁定用户eos（账户eos余额-本次购买eos金额，新增冻结金额，修改状态：扣款中，已扣款）
        sql = "SELECT available from accounting_balance where user_id = ? and currency_name='EOSRAM'";
        DataMap eosramBalance = data.queryOne(sql, userId);
        BigDecimal eosramAvailable = BigDecimal.ZERO;
        //用户eosram账户余额
        if (eosramBalance != null)
            eosramAvailable = eosramBalance.getBig("available") == null ? BigDecimal.ZERO : eosramBalance.getBig("available");
        String order_id = ConstantUtils.getId();
        //TODO 创建订单
        sql = "INSERT INTO order_eosram(id,user_id,eos_amount,eosram_amount,eos_number,inorout) VALUES( ?, ?, ?, ?, ?, ?)";
        int rowN = data.update(sql, order_id,userId,eosavailable,eosramAvailable,eos_number,"in");
        //TODO 减少用户账号中心的eos余额
        sql = "UPDATE accounting_balance SET available = available - ?  WHERE user_id = ? AND currency_name = 'EOS' AND available = ?";
        rowN = data.update(sql, eos_number, userId, eosavailable);
        if (rowN != 1)
            throw new RuntimeException("update_accounting_blance_fail");

        //TODO 请求钱包发起购买请求（）
        // 查询用memo
        sql = "SELECT address FROM accounting_deposit_address WHERE user_id = ? AND chain_name = ? AND currency_name = ?";
        DataMap address = data.queryOne(sql, userId, "EOS", "EOS");
        if (address == null)
            throw new RuntimeException("account_deposit_address_eos_is_null");
        //String memo = ConstantUtils.getMemoByAddress(address.getString("address"));
        String memo = address.getString("address");
        JSONObject post = null;
        try {
            post = httpService.post2(walletConfig.getUrl5555()+ConstantUtils.BUYEOSRAM, "id",order_id, "memo",memo, "eos",eos_number);

            if(post !=null){
                //BigDecimal eos = (BigDecimal)(post.get("eos"));

                BigDecimal eosRam = post.getBigDecimal("ram");
                BigDecimal eosram_transaction_price = new BigDecimal(post.get("price")+"");

                String txid =  post.getString("chain_txid");//chain_txid
                //TODO 判断是否购买成功，1，购买成功新增用户ram余额，同时把订单表状态修改为已购买，2，购买失败回滚用户账户金额
                if( txid != null ){
                    sql = "UPDATE order_eosram SET eosram_number = ? ,state = ?, eosram_transaction_price = ? ,txid = ? WHERE user_id = ? AND id = ?";
                    rowN = data.update(sql, eosRam, 1, eosram_transaction_price,txid,userId,order_id);
                    if (rowN != 1)
                        throw new RuntimeException("update_order_eosram_fail");

                    if (eosramBalance == null) {
                        //1，钱包购买成功，如果用户是第一次购买，新增一条用户eosram账户记录
                        sql = "INSERT INTO accounting_balance(user_id, currency_name, available) VALUES (?, ?, ?) ";
                        rowN = data.update(sql, userId, "EOSRAM", eosRam);
                        if (rowN < 1) {
                            throw new RuntimeException("balance concurrent access");
                        }
                    }else {
                        // 2，钱包购买成功，更新用户eosram账户余额
                        sql = "UPDATE accounting_balance SET available = available + ?  WHERE user_id = ? AND currency_name = 'EOSRAM' AND available = ?";
                        rowN = data.update(sql, eosRam, userId, eosramAvailable);
                        if (rowN != 1)
                            throw new RuntimeException("update_accounting_blance_fail");
                    }
                }
            }
            return post;
        } catch (Exception e) {
            // 购买失败退回用户eos账户数量
//            sql = "UPDATE accounting_balance SET available = available + ?  WHERE user_id = ? AND currency_name = 'EOS' AND available = ?";
//            rowN = data.update(sql, eos_number, userId, eosramAvailable);
//            if (rowN != 1)
//                throw new RuntimeException("update_accounting_blance_fail");

            throw new RuntimeException("buyEosRam_fail"+e.getMessage());
        }

    }


    /**
     * 出售eosram
     *
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public JSONObject sellEosRam(Long userId, BigDecimal eosram){
        //        //TODO 查询用户eosram金额是否足够，不够提示余额不足，结束
        String sql = "SELECT available from accounting_balance where user_id = ? and currency_name='EOSRAM'";
        DataMap balance = data.queryOne(sql, userId);
        if (balance == null)
            throw new RuntimeException("balace_not_enough");
        //用户eosram账户余额
        BigDecimal eosRamAvailable = balance.getBig("available");
        if (eosRamAvailable.compareTo(BigDecimal.ZERO) == 0)
            throw new RuntimeException("balace_not_enough");
        //TODO 锁定用户eos（账户eos余额-本次购买eos金额，新增冻结金额，修改状态：扣款中，已扣款）
        sql = "SELECT available from accounting_balance where user_id = ? and currency_name='EOS'";
        DataMap eosBalance = data.queryOne(sql, userId);
        BigDecimal eosAvailable = BigDecimal.ZERO;
        //用户eosram账户余额
        if (eosBalance != null)
            eosAvailable = eosBalance.getBig("available") == null ? BigDecimal.ZERO : eosBalance.getBig("available");
        String order_id = (Math.random()+"").substring(2,16);
        //TODO 创建订单
        sql = "INSERT INTO order_eosram(id,user_id,eos_amount,eosram_amount,eosram_number,eos_number,inorout) VALUES( ?, ?, ?, ?, ?, ?, ?)";
        int rowN = data.update(sql, order_id,userId,eosAvailable,eosRamAvailable,eosram,0,"out");

        //TODO 减少用户账号中心的eos余额
        sql = "UPDATE accounting_balance SET available = available - ?  WHERE user_id = ? AND currency_name = 'EOSRAM' AND available = ?";
        rowN = data.update(sql, eosram, userId, eosRamAvailable);
        if (rowN != 1)
            throw new RuntimeException("update_accounting_blance_fail");

        //TODO 请求钱包发起购买请求（）
        // 查询用memo
        sql = "SELECT address FROM accounting_deposit_address WHERE user_id = ? AND chain_name = ? AND currency_name = ?";
        DataMap address = data.queryOne(sql, userId, "EOS", "EOS");
        String memo = address.getString("address");
        JSONObject post = null;
        try {
            post = httpService.post2(walletConfig.getUrl5555()+ConstantUtils.SELLEOSRAM, "id",order_id, "memo",memo, "ram",eosram);

            if(post !=null){
                String txid =  post.getString("chain_txid");//chain_txid
                if(txid != null){
                    BigDecimal eos = post.getBigDecimal("eos");
                    BigDecimal eosRam = post.getBigDecimal("ram");
                    BigDecimal eosram_transaction_price = post.getBigDecimal("price");//(KB)
                    //TODO 判断是否购买成功，1，购买成功新增用户ram余额，同时把订单表状态修改为已购买，2，购买失败返回用户账户金额
                    sql = "UPDATE order_eosram SET eos_number = ? ,state = ?, eosram_transaction_price = ? ,txid = ? WHERE user_id = ? AND id = ?";
                    rowN = data.update(sql, eos, 1, eosram_transaction_price,txid,userId,order_id);

                    if (rowN != 1)
                        throw new RuntimeException("update_order_eosram_fail");

                    sql = "UPDATE accounting_balance SET available = available + ?  WHERE user_id = ? AND currency_name = 'EOS' AND available = ?";
                    rowN = data.update(sql, eos, userId, eosAvailable);
                    if (rowN != 1)
                        throw new RuntimeException("update_accounting_blance_fail");
                } else {
                    throw new RuntimeException("wallet_response_null");
                }

            } else {
                throw new RuntimeException("wallet_response_null");
            }

            return post;
        } catch (Exception e) {
            throw new RuntimeException("wallet_response_fail"+e.getMessage());
        }

    }
}
