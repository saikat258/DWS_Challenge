package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Service
public class AccountsService {

    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    private static final String TRANSFER_DESC_TO_DEBTOR = "Your account has been debited by $ from account ";

    private static final String TRANSFER_DESC_TO_CREDITOR = "UPDATE: $ has been debited from your account as a payment to account ";

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public List<Account> doTransfer(String accountFromId, String accountToId, BigDecimal amount) {
        List<Account> accounts = new ArrayList<>();
        try {
            Account fromAcc = getAccount(accountFromId);
            Account toAcc = getAccount(accountToId);
            if (fromAcc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                calculateDoTransfers(fromAcc, toAcc, amount);
            }
            accounts.add(this.accountsRepository.getAccount(accountFromId));
            accounts.add(this.accountsRepository.getAccount(accountToId));
            log.info("accountList accounts.size() {}", accounts.size());
            sendNotifications(fromAcc, toAcc, amount);
        } catch (Exception e) {
            log.error("Exception occurred while transfers {}", (Object) e.getStackTrace());
        }
        return accounts;
    }

    synchronized void calculateDoTransfers(Account fromAcc,
                                           Account toAcc, BigDecimal amount) {
        BigDecimal balanceInFromAcc = fromAcc.getBalance();
        BigDecimal balanceInToAcc = toAcc.getBalance();
        log.info("balanceInFromAcc = {}, balanceInToAcc = {}", balanceInFromAcc, balanceInToAcc);
        if (balanceInFromAcc.compareTo(amount) > 0) {
            balanceInFromAcc = balanceInFromAcc.subtract(amount);
            balanceInToAcc = balanceInToAcc.add(amount);
            fromAcc.setBalance(balanceInFromAcc);
            this.accountsRepository.updateAccount(fromAcc);
            toAcc.setBalance(balanceInToAcc);
            this.accountsRepository.updateAccount(toAcc);
        }
        log.info("transfer complete, accounts updated");
    }

    private void sendNotifications(Account fromAcc, Account toAcc, BigDecimal amount) {
        try {
            this.notificationService.notifyAboutTransfer(toAcc,
                    TRANSFER_DESC_TO_DEBTOR.replace("$", String.valueOf(amount)) + fromAcc.getAccountId());
            this.notificationService.notifyAboutTransfer(fromAcc,
                    TRANSFER_DESC_TO_CREDITOR.replace("$", String.valueOf(amount)) + toAcc.getAccountId());
            log.info("Notifications sent");
        } catch (Exception e) {
            log.error("Exception occurred while sending notifications {}", (Object) e.getStackTrace());
        }
    }
}
