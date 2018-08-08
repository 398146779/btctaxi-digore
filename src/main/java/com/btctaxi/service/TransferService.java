package com.btctaxi.service;

import com.btctaxi.config.WalletConfig;
import com.btctaxi.controller.support.ServiceError;
import com.btctaxi.dao.AccountingBalanceRepository;
import com.btctaxi.dao.AccountingTransferRepository;
import com.btctaxi.domain.AccountingBalance;
import com.btctaxi.domain.AccountingTransfer;
import com.btctaxi.enums.TransferSide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.btctaxi.controller.support.ErrorCodeEnum.TRANSFER_BALANCE_NOT_ENOUGH;

@Service
@Slf4j
public class TransferService {

    @Autowired
    private AccountingTransferRepository accountingTransferRepository;

    @Autowired
    AccountingBalanceRepository accountingBalanceRepository;

    @Autowired
    WalletConfig allowTransfer;

    /**
     * 列表页
     *
     * @param userId
     * @param transferSide
     * @param page
     * @param size
     * @return
     */
    public Page<AccountingTransfer> list(Long userId, TransferSide transferSide, Integer page, Integer size) {
        PageRequest pageable = new PageRequest(page, size);
        if (TransferSide.BUY == transferSide) {
            return accountingTransferRepository.findALLByToUserId(userId, pageable);
        } else if (TransferSide.SELL == transferSide) {
            return accountingTransferRepository.findALLByFromUserId(userId, pageable);
        }
        return null;
    }


    @Transactional(rollbackFor = Throwable.class)
    public Boolean create(Long fromUserId, Long toUserId, String currencyName, BigDecimal amount, BigDecimal fee, String memo) {

        if (!"123".equals(allowTransfer.getAllowTransfer())) {
            log.info("由于开关为非设定值--->关闭转账");
        }
        //余额不足
        if (amount.compareTo(BigDecimal.ZERO) < 1) ServiceError.ofAndThrow(TRANSFER_BALANCE_NOT_ENOUGH);

        AccountingBalance fromBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(fromUserId, currencyName);
        AccountingBalance toBalance = accountingBalanceRepository.findByUserIdAndCurrencyName(toUserId, currencyName);

        //余额不足
        if (fromBalance == null) ServiceError.ofAndThrow(TRANSFER_BALANCE_NOT_ENOUGH);
        if (toBalance == null) ServiceError.ofAndThrow(TRANSFER_BALANCE_NOT_ENOUGH);

        BigDecimal available = fromBalance.getAvailable();

        //余额不足
        if (fromBalance.getAvailable().compareTo(amount) < 0) ServiceError.ofAndThrow(TRANSFER_BALANCE_NOT_ENOUGH);


        fromBalance.setAvailable(available.subtract(amount).subtract(fee)); // --
        toBalance.setAvailable(toBalance.getAvailable().add(amount)); // ++


        accountingBalanceRepository.saveAndFlush(fromBalance);
        accountingBalanceRepository.saveAndFlush(toBalance);

        AccountingTransfer transfer = AccountingTransfer.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .currencyName(currencyName)
                .amount(amount)
                .fromUserLeftAmount(fromBalance.getAvailable())
                .toUserLeftAmount(toBalance.getAvailable())
                .fee(fee)
                .memo(memo)
                .build();

        accountingTransferRepository.save(transfer);

        return true;
    }
}


