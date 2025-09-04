package com.enoc.transaction.model;

import com.enoc.transaction.enums.StatusEnum;
import com.enoc.transaction.enums.TransactionOrigin;
import com.enoc.transaction.enums.TransactionType;
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
    private String accountId;
    private String operationTypeId;
    private LocalDateTime eventDate;
    private String destinationAccountId;
    private TransactionType type;
    private TransactionOrigin origin;
    private BigDecimal amount;
    private BigDecimal commissionApplied;
    private LocalDateTime date;
    private String description;
    private StatusEnum status;


}

