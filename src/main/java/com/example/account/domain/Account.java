package com.example.account.domain;

import com.example.account.type.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AccountUser accountUser;

    private String accountNumber;
    private String accountPassword;

    private Long balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private String accountName;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;
}
