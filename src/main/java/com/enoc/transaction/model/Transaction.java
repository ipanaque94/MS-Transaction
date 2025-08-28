package com.enoc.transaction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @BsonId
    private String id;
    private String customerId;
    private String productId;
    private String destinationAccountId;
    private TransactionType type;
    private TransactionOrigin origin;
    private BigDecimal amount;
    private BigDecimal commissionApplied;
    private LocalDateTime date;
    private String description;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        PAYMENT,
        CREDIT_CHARGE,
        TRANSFER_INTERNAL,
        TRANSFER_EXTERNAL

    }

    public enum TransactionOrigin {
        INTERNAL,
        EXTERNAL
    }

}

