package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
}
