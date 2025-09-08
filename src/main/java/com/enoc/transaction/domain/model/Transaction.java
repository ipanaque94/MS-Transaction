package com.enoc.transaction.domain.model;

import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
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
    private String debtorDni; // DNI del titular de la deuda
    private String payerDni;  // DNI del tercero que paga
    private String productId;
    private String accountId;
    private String operationTypeId;
    private OffsetDateTime eventDate;
    private String destinationAccountId;
    private TransactionType type;
    private TransactionOrigin origin;
    private TransactionState state = TransactionState.ACTIVE;
    private BigDecimal amount;
    private BigDecimal commissionApplied;
    private OffsetDateTime date;
    private String description;
    private StatusEnum status;
    @CreatedDate
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;


}

