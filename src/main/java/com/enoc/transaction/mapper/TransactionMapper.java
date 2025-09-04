package com.enoc.transaction.mapper;

import com.enoc.transaction.enums.StatusEnum;
import com.enoc.transaction.enums.TransactionOrigin;
import com.enoc.transaction.enums.TransactionType;
import com.enoc.transaction.model.Transaction;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        if (request == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setProductId(request.getProductId());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setAccountId(request.getAccountId());
        transaction.setOperationTypeId(request.getOperationTypeId());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription().orElse(null));
        transaction.setDestinationAccountId(request.getDestinationAccountId().orElse(null));
        transaction.setCommissionApplied(request.getCommissionApplied().orElse(null));

        if (request.getEventDate() != null) {
            transaction.setEventDate(request.getEventDate().toLocalDateTime());
        }

        if (request.getDate() != null) {
            transaction.setDate(request.getDate().toLocalDateTime());
        }

        if (request.getType() != null) {
            transaction.setType(TransactionType.valueOf(request.getType().toString()));
        }

        if (request.getOrigin() != null) {
            transaction.setOrigin(TransactionOrigin.valueOf(request.getOrigin().toString()));
        }

        if (request.getStatus() != null) {
            transaction.setStatus(StatusEnum.valueOf(request.getStatus().toString()));
        }

        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setProductId(transaction.getProductId());
        response.setCustomerId(transaction.getCustomerId());
        response.setAccountId(transaction.getAccountId());
        response.setOperationTypeId(transaction.getOperationTypeId());
        response.setAmount(transaction.getAmount());
        response.setDescription(JsonNullable.of(transaction.getDescription()));
        response.setDestinationAccountId(JsonNullable.of(transaction.getDestinationAccountId()));
        response.setCommissionApplied(JsonNullable.of(transaction.getCommissionApplied()));

        if (transaction.getEventDate() != null) {
            response.setEventDate(OffsetDateTime.of(transaction.getEventDate(), ZoneOffset.UTC));
        }

        if (transaction.getDate() != null) {
            response.setDate(OffsetDateTime.of(transaction.getDate(), ZoneOffset.UTC));
        }

        if (transaction.getType() != null) {
            response.setType(TransactionResponse.TypeEnum.valueOf(transaction.getType().name()));
        }

        if (transaction.getOrigin() != null) {
            response.setOrigin(TransactionResponse.OriginEnum.valueOf(transaction.getOrigin().name()));
        }

        if (transaction.getStatus() != null) {
            try {
                response.setStatus(TransactionResponse.StatusEnum.valueOf(transaction.getStatus().name()));
            } catch (IllegalArgumentException ex) {
                response.setStatus(null);
            }
        }

        return response;
    }
}