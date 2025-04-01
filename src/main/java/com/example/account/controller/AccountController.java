package com.example.account.controller;

import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request req
    ){
        return CreateAccount.Response.from(
                accountService.createAccount(
                        req.getUserId(),
                        req.getAccountPassword(),
                        req.getInitialBalance(),
                        req.getAccountName()
                )
        );
    }

    @DeleteMapping
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ){
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber(),
                        request.getAccountPassword()
                )
        );
    }
}
