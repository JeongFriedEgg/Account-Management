package com.example.account.dto;

import com.example.account.serializer.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class CreateAccount {
    @Getter
    @Setter
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull(message = "계좌 비밀번호는 필수입니다.")
        @Size(min = 4, max = 4, message = "계좌 비밀번호의 자리수는 4자리 입니다.")
        private String accountPassword;

        @NotNull(message = "초기 잔액은 필수입니다.")
        @Min(value = 0, message = "초기 잔액은 0원 이상이어야 합니다.")
        private Long initialBalance;

        @Size(max = 10, message = "계좌명은 최대 10자리까지 가능합니다.")
        private String accountName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Response {
        private Long userId;
        private String accountNumber;
        private String accountName;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime registeredAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .accountName(accountDto.getAccountName())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
