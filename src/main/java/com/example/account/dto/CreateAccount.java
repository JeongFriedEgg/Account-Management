package com.example.account.dto;

import com.example.account.serializer.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

        @NotNull
        private String accountPassword;

        @NotNull
        @Min(0)
        private Long initialBalance;

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
