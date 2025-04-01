package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class CancelBalance {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "거래 아이디는 필수입니다.")
        private String transactionId;

        @NotNull(message = "계좌번호는 필수입니다.")
        @Size(min = 10, max = 10, message = "계좌번호의 자리수는 10자리 입니다.")
        private String accountNumber;

        @NotNull(message = "거래 금액은 필수입니다.")
        @Min(value = 1000, message = "최소 거래금액은 1000원 입니다.")
        @Max(value = 100_000_000, message = "최대 거래금액은 1억원 입니다.")
        private Long amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
