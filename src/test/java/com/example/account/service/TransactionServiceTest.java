package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Test
    void useBalance_Success() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountNumber("1000000012")
                .accountPassword("1234")
                .accountStatus(IN_USE)
                .balance(10000L)
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.useBalance(1L,"1000000000","1234",200L);
        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L,captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(9000L,transactionDto.getBalanceSnapshot());
        assertEquals(1000L,transactionDto.getAmount());
    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    void useBalance_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L,"1000000000","1234",1000L));
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    void useBalance_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "10000000000","1234",1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void useBalance_userMismatch() {
        //given
        AccountUser user1 = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        AccountUser user2 = AccountUser.builder()
                .id(11L)
                .name("Chicken")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user1));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user2)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890","1234",1000L));
        //then
        assertEquals(USER_ACCOUNT_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 비밀번호 다름 - 잔액 사용 실패")
    void useBalance_AccountPasswordMismatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .accountPassword("1234")
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(10L,"1000000012","2345",1000L));
        // then
        assertEquals(ACCOUNT_PASSWORD_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌가 이미 해지됨 - 잔액 사용 실패")
    void useBalance_AlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(100L)
                        .accountNumber("1000000012")
                        .accountPassword("1234")
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L,"1234567890","1234",1000L));
        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }
    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우 - 잔액 사용 실패")
    void useBalance_ExceedAmount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg").build();
//        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(1000L)
                .accountPassword("1234")
                .accountNumber("1000000012").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", "1234",10000L));
        //then
        verify(transactionRepository, times(0)).save(any());
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
    }
}