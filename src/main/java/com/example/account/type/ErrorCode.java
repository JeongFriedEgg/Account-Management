package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 900
    VALIDATION_FAILED(999, "잘못된 입력 값입니다."),
    // 10XX : 사용자
    USER_NOT_FOUND(1000,"사용자가 없습니다."),


    // 11XX : 계좌
    MAX_ACCOUNT_PER_USER_10(1100,"사용자 최대 계좌는 10개입니다."),
    MAX_ACCOUNT_NAME_LEN_10(1101,"계좌명 최대 길이는 10자리 입니다."),
    ACCOUNT_NOT_FOUND(1102,"계좌가 없습니다."),
    USER_ACCOUNT_MISMATCH(1103,"사용자와 계좌의 소유주가 일치하지 않습니다."),
    ACCOUNT_PASSWORD_MISMATCH(1104,"계좌 비밀번호가 일치하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED(1105,"계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY(1106,"잔액이 있는 계좌는 해지할 수 없습니다."),
    AMOUNT_EXCEED_BALANCE(1107,"거래 금액이 잔액보다 큽니다."),

    // 12XX : 거래
    TRANSACTION_NOT_FOUND(1200,"해당 거래가 없습니다."),
    TRANSACTION_ACCOUNT_MISMATCH(1201,"이 거래는 해당 계좌에서 발생한 거래가 아닙니다."),
    TRANSACTION_AMOUNT_MISMATCH(1202,"거래금액과 거래 취소금액이 일치하지 않습니다."),
    TOO_OLD_TRANSACTION_TO_CANCEL(1203,"1년이 지난 거래는 취소가 불가능합니다."),
    ;

    private final int status;
    private final String description;
}
