package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class UseBalance {
    @Getter
    @Setter
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull(message = "계좌번호는 필수입니다.")
        @Size(min = 10, max = 10, message = "계좌번호의 자리수는 10자리 입니다.")
        private String accountNumber;

        @NotNull(message = "계좌 비밀번호는 필수입니다.")
        @Size(min = 4, max = 4, message = "계좌 비밀번호의 자리수는 4자리 입니다.")
        private String accountPassword;

        @NotNull(message = "거래 금액은 필수입니다.")
        @Min(value = 1000, message = "최소 거래금액은 1000원 입니다.")
        @Max(value = 100_000_000, message = "최대 거래금액은 1억원 입니다.")
        private Long amount;
    }

    @Getter
    @Setter
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactionAt;

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResult(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactionAt(transactionDto.getTransactedAt())
                    .build();
        }
    }
}
