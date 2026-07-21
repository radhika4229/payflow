package com.radhika.payflow.audit.aspect;

import com.radhika.payflow.audit.annotation.Auditable;
import com.radhika.payflow.audit.entity.AuditLog;
import com.radhika.payflow.audit.repository.AuditLogRepository;
import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @AfterReturning(value = "@annotation(auditable)", argNames = "joinPoint,auditable")

    public void logAudit(JoinPoint joinPoint, Auditable auditable){
        System.out.println("AOP EXECUTED");
        String action= auditable.action();
      String methodName = joinPoint.getSignature().getName();
      User user= null;
      try{
          String email= securityUtil.getCurrentUserEmail();
          user=userRepository.findByEmail(email).orElse(null);

      }
      catch(Exception e){}

AuditLog log =  AuditLog.builder()
        .user(user)
                .action(action)
                .details("Method : " + methodName)
                .build();

        System.out.println("Action = " + action);
        System.out.println("User = " + user);
        auditLogRepository.save(log);

        System.out.println("Audit log saved");



    }
}
