package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    public AccountDto createAccount(Long userId, String accountPassword, Long initialBalance, String accountName) {
        if (accountName.length() > 10){
            throw new AccountException(MAX_ACCOUNT_NAME_LEN_10);
        }
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        if (accountRepository.countByAccountUser(accountUser) == 10){
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        accountName = Optional.of(accountName)
                                .filter(name -> !name.trim().isEmpty())
                                .orElseGet(accountUser::getName);

        return AccountDto.fromEntity(
                accountRepository.save(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber(newAccountNumber)
                                .accountPassword(accountPassword)
                                .balance(initialBalance)
                                .accountStatus(IN_USE)
                                .accountName(accountName)
                                .registeredAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    public AccountDto deleteAccount(Long userId, String accountNumber, String accountPassword){
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        if (!accountUser.getId().equals(account.getAccountUser().getId())){
            throw new AccountException(USER_ACCOUNT_MISMATCH);
        }
        if (!accountPassword.equals(account.getAccountPassword())){
            throw new AccountException(ACCOUNT_PASSWORD_MISMATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED){
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0){
            throw new AccountException(BALANCE_NOT_EMPTY);
        }

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());
        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }
}
