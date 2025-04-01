package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static com.example.account.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("id 값을 가장 낮은 값의 다음 값으로 부여하여 계좌 생성")
    void createAccount_Success() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000012")
                                .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(
                1L,"1234",1000L,"");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(10L, accountDto.getUserId());
        assertEquals("1000000013",captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌 생성 시 계좌 이름 설정이 없는 경우, 사용자의 이름을 계좌명으로 사용")
    void createAccount_AccountNameIsEmpty_UsesAccountUserName() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000012")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013")
                        .accountName("Egg")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(
                10L, "1234", 1000L, "");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals("Egg", captor.getValue().getAccountName());
        assertEquals(10L, accountDto.getUserId());
        assertEquals("1000000013", accountDto.getAccountNumber());
        assertEquals("Egg", accountDto.getAccountName());
    }

    @Test
    @DisplayName("계좌명을 최대 10자리까지 입력받아 입력받은 계좌명으로 계좌 생성")
    void createAccount_AccountNameWithValidLength_CanBeCreated() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();

        String validAccountName = "Egg123#45e";

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000012")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013")
                        .accountName(validAccountName)
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(
                10L, "1234", 1000L, validAccountName);

        // when
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(validAccountName, captor.getValue().getAccountName());

        assertNotNull(accountDto);
        assertEquals(10L, accountDto.getUserId());
        assertEquals("1000000013", accountDto.getAccountNumber());
        assertEquals(validAccountName, accountDto.getAccountName());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 계좌 생성시 실패")
    void createAccount_UserNotFound_Fails() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(
                        1L,"1234", 1000L, "Test"));
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌명을 10자 초과하여 입력하면 실패")
    void createAccount_AccountNameTooLong_Fails() {
        // given
        String longAccountName = "Egg123#45e3";
        // when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(
                        10L,"1234",1000L,longAccountName
                ));
        // then
        assertEquals(MAX_ACCOUNT_NAME_LEN_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자가 10개 이상의 계좌를 보유하면 계좌 생성 실패")
    void createAccount_MaxAccountsPerUser_Fails() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.countByAccountUser(accountUser))
                .willReturn(10);
        // when
        AccountException exception = assertThrows(AccountException.class, () ->
                accountService.createAccount(
                        10L,"1234",1000L, "NewAccount"
                ));
        // then
        assertEquals(MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }
}