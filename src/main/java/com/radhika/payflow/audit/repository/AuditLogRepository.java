package com.radhika.payflow.audit.repository;

import com.radhika.payflow.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

}
