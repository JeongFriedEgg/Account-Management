package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/use")
    public UseBalance.Response useBalance(
            @RequestBody @Valid UseBalance.Request request
    ){
        try {
            return UseBalance.Response.from(
                    transactionService.useBalance(
                            request.getUserId(),
                            request.getAccountNumber(),
                            request.getAccountPassword(),
                            request.getAmount())
            );
        }catch (AccountException e){
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @PostMapping("/cancel")
    public CancelBalance.Response cancelBalance(
            @RequestBody @Valid CancelBalance.Request request
    ){
        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(request.getTransactionId(),
                            request.getAccountNumber(), request.getAmount())
            );
        }catch (AccountException e){
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }
}
