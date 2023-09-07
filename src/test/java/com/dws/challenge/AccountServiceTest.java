package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
public class AccountServiceTest {

    @InjectMocks
    AccountsService accountsService;

    @Mock
    AccountsRepository accountsRepository;

    Account fromAcc;

    Account toAcc;

    @BeforeEach
    public void init() {
        fromAcc = new Account("123A001", BigDecimal.valueOf(120000));
        toAcc = new Account("123A002", BigDecimal.valueOf(100000));
    }

    @Test
    public void createAccountTest() {
        Mockito.doNothing().when(accountsRepository).createAccount(fromAcc);
        Mockito.doNothing().when(accountsRepository).createAccount(toAcc);
        accountsService.createAccount(fromAcc);
        accountsService.createAccount(toAcc);
    }

    @Test
    public void getAccountTest() {
        Mockito.when(accountsRepository.getAccount(fromAcc.getAccountId())).thenReturn(fromAcc);
        Account acc = accountsService.getAccount(fromAcc.getAccountId());
        assertEquals(acc.getAccountId(), "123A001");
    }

    @Test
    public void doTransferTest() throws Exception {
        Mockito.when(accountsRepository.getAccount(fromAcc.getAccountId())).thenReturn(fromAcc);
        Mockito.when(accountsRepository.getAccount(toAcc.getAccountId())).thenReturn(toAcc);
        Mockito.doNothing().when(accountsRepository).updateAccount(fromAcc);
        Mockito.doNothing().when(accountsRepository).updateAccount(toAcc);
        List<Account> accounts = accountsService.doTransfer(fromAcc.getAccountId(),
                toAcc.getAccountId(), BigDecimal.valueOf(10000));
        Assertions.assertFalse(accounts.isEmpty());
        assertEquals(accounts.get(0).getBalance(), BigDecimal.valueOf(110000));
    }

    @Test
    public void withdrawElseCaseTest(CapturedOutput output) throws Exception {
        Account fromAcc = new Account("123A001", BigDecimal.valueOf(5000));
        Mockito.doNothing().when(accountsRepository).updateAccount(fromAcc);
        accountsService.withdraw(fromAcc, BigDecimal.valueOf(10000));
        Assertions.assertTrue(output.getOut().contains("Insufficient balance in account"));
    }

    @AfterAll
    static void tearDownAll() {
    }
}
