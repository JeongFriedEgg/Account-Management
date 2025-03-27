package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 10XX : 사용자
    USER_NOT_FOUND(1000,"사용자가 없습니다."),


    // 11XX : 계좌
    MAX_ACCOUNT_PER_USER_10(1100,"사용자 최대 계좌는 10개입니다."),
    MAX_ACCOUNT_NAME_LEN_10(1101,"계좌명 최대 길이는 10자리 입니다.")

    ;

    private final int status;
    private final String description;
}
