package com.example.account.exception;

import com.example.account.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountException extends RuntimeException {
    private int status;
    private ErrorCode errorCode;
    private String errorMessage;
    public AccountException(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
