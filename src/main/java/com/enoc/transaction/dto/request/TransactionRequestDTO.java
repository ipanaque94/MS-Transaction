package com.enoc.transaction.dto.request;

import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TransactionRequestDTO {
    private String customerId;
    private String debtorDni;   // DNI del titular de la deuda
    private String payerDni;    // DNI del tercero que paga
    private String productId;
    private String accountId;
    private String operationTypeId;
    private String destinationAccountId;
    private TransactionType type;
    private TransactionOrigin origin;
    private StatusEnum status;
    private BigDecimal amount;
    private BigDecimal commissionApplied;
    private OffsetDateTime date;
    private OffsetDateTime eventDate;
    private String description;

}
