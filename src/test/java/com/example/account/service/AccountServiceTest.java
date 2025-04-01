package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.account.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccount_Success() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(user)
                                .accountNumber("1000000012")
                                .accountPassword("1234")
                                .balance(0L)
                        .build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        // when
        AccountDto accountDto = accountService.deleteAccount(
                1L,"1234567890","1234");
        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000012",captor.getValue().getAccountNumber());
        assertEquals("1234",captor.getValue().getAccountPassword());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L,"1234567890","1234"));
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L,"1234567890","1234"));
        // then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 계좌 해지 실패")
    void deleteAccount_UserAccountMismatch() {
        // given
        AccountUser user1 = AccountUser.builder()
                .id(11L)
                .name("Egg")
                .build();
        AccountUser user2 = AccountUser.builder()
                .id(12L)
                .name("Chicken")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user1));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(user2)
                                .balance(0L)
                                .accountNumber("1000000012")
                                .accountPassword("1234")
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L,"1234567890","2345"));
        // then
        assertEquals(USER_ACCOUNT_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 비밀번호 다름 - 계좌 해지 실패")
    void deleteAccount_AccountPasswordMismatch() {
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
                () -> accountService.deleteAccount(10L,"1000000012","2345"));
        // then
        assertEquals(ACCOUNT_PASSWORD_MISMATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌에 잔액이 남아있음 - 계좌 해지 실패")
    void deleteAccount_BalanceNotEmpty() {
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
                                .accountNumber("1000000012")
                                .accountPassword("1234")
                                .balance(100L)
                        .build()));
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890", "1234"));
        // then
        assertEquals(BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("해지된 계좌는 해지할 수 없음 - 계좌 해지 실패")
    void deleteAccount_AlreadyUnRegistered() {
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
                () -> accountService.deleteAccount(1L,"1234567890","1234"));
        // then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 확인 성공")
    void getAccountsInfoByUserId_Success() {
        // given
        AccountUser user = AccountUser.builder()
                .id(10L)
                .name("Egg")
                .build();
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .accountName("Egg")
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .accountName("EggName")
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .accountName("Egg")
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        // when
        List<AccountDto> accountDtos = accountService.getAccountsInfoByUserId(1L);
        // then
        assertEquals(3,accountDtos.size());
        assertEquals("1111111111",accountDtos.get(0).getAccountNumber());
        assertEquals(1000,accountDtos.get(0).getBalance());
        assertEquals("Egg",accountDtos.get(0).getAccountName());
        assertEquals("2222222222",accountDtos.get(1).getAccountNumber());
        assertEquals(2000,accountDtos.get(1).getBalance());
        assertEquals("EggName",accountDtos.get(1).getAccountName());
        assertEquals("3333333333",accountDtos.get(2).getAccountNumber());
        assertEquals(3000,accountDtos.get(2).getBalance());
        assertEquals("Egg",accountDtos.get(2).getAccountName());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 확인 실패")
    void getAccountsInfoByUserId_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountsInfoByUserId(1L));
        // then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }
}