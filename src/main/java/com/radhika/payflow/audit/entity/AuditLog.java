package com.radhika.payflow.audit.entity;

import com.radhika.payflow.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuditLog {



        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;

        @Column(nullable = false, length = 100)
        private String action;

        @Column(columnDefinition = "TEXT")
        private String details;

        @Column(name = "ip_address", length = 50)
        private String ipAddress;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;
    }






