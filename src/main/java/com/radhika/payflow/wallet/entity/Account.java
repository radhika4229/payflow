package com.radhika.payflow.wallet.entity;

import com.radhika.payflow.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false,unique=true)
    private User user;

    @Column(nullable=false,precision=15,scale=2)
    @Builder.Default
    private BigDecimal balance=BigDecimal.ZERO;

    @Version
    @Column(nullable=false)
    @Builder.Default
    private Long version=0L;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
