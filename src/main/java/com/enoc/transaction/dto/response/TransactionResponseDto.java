package com.enoc.transaction.dto.response;

import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TransactionResponseDto {
    private String id;
    private String debtorDni;
    private String payerDni;
    private String customerId;
    private String productId;
    private String accountId;
    private String operationTypeId;
    private String destinationAccountId;
    private TransactionType type;
    private TransactionOrigin origin;
    private StatusEnum status;
    private TransactionState state = TransactionState.ACTIVE;
    private BigDecimal amount;
    private BigDecimal commissionApplied;
    private OffsetDateTime date;
    private OffsetDateTime eventDate;
    private String description;

}