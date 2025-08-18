package com.enoc.transaction.model;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.codecs.pojo.annotations.BsonId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @BsonId
    private String id;

    private String productId; // FK to Product

    private TransactionType type;

    private BigDecimal amount;

    private LocalDateTime date;

    private String description;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        PAYMENT,
        CREDIT_CHARGE
    }
}

