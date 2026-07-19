package com.radhika.payflow.transaction.event;

import com.radhika.payflow.transaction.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionEvent {
   UUID transactionId;
   UUID senderAccountId;
 UUID receiverAccountId;
 BigDecimal amount;
 TransactionType type;
 LocalDateTime timestamp;



}




