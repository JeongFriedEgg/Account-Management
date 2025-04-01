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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, String accountPassword, Long amount){
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
        if (account.getAccountStatus() != AccountStatus.IN_USE){
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount){
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }

        account.useBalance(amount);
        return TransactionDto.fromEntity(
                transactionRepository.save(
                        Transaction.builder()
                                .account(account)
                                .transactionType(USE)
                                .transactionResultType(S)
                                .amount(amount)
                                .balanceSnapshot(account.getBalance())
                                .transactionId(UUID.randomUUID().toString().replace("-",""))
                                .transactedAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(F)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount){
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        if (!transaction.getAccount().getId().equals(account.getId())){
            throw new AccountException(TRANSACTION_ACCOUNT_MISMATCH);
        }
        if (!transaction.getAmount().equals(amount)){
            throw new AccountException(TRANSACTION_AMOUNT_MISMATCH);
        }
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))){
            throw new AccountException(TOO_OLD_TRANSACTION_TO_CANCEL);
        }

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                transactionRepository.save(
                        Transaction.builder()
                                .transactionType(CANCEL)
                                .transactionResultType(S)
                                .account(account)
                                .amount(amount)
                                .balanceSnapshot(account.getBalance())
                                .transactionId(UUID.randomUUID().toString().replace("-",""))
                                .transactedAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
        transactionRepository.save(
                Transaction.builder()
                        .transactionType(CANCEL)
                        .transactionResultType(F)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }
}
