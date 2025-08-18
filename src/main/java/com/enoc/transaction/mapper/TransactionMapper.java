package com.enoc.transaction.mapper;

import com.enoc.transaction.model.Transaction;
import com.enoc.transaction.model.TransactionRequest;
import com.enoc.transaction.model.TransactionResponse;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.OffsetDateTime;

@NoArgsConstructor
public final class TransactionMapper {

    public static Transaction toEntity(TransactionRequest request) {
        if (request == null) return null;

        Transaction transaction = new Transaction();

        transaction.setId(null);
        transaction.setProductId(request.getProductId());

        if (request.getType() != null) {
            transaction.setType(Transaction.TransactionType.valueOf(request.getType().getValue()));
        }

        transaction.setAmount(request.getAmount());

        if (request.getDate() != null) {
            transaction.setDate(request.getDate().toLocalDateTime());
        }

        transaction.setDescription(request.getDescription().orElse(null));

        return transaction;
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setProductId(transaction.getProductId());

        if (transaction.getType() != null) {
            response.setType(TransactionResponse.TypeEnum.fromValue(transaction.getType().name()));
        }

        response.setAmount(transaction.getAmount());

        if (transaction.getDate() != null) {
            response.setDate(transaction.getDate().atOffset(OffsetDateTime.now().getOffset()));
        }

        response.setDescription(transaction.getDescription() != null
                ? JsonNullable.of(transaction.getDescription())
                : JsonNullable.undefined());

        return response;
    }

    public static Transaction toEntity(TransactionResponse response) {
        if (response == null) return null;

        Transaction transaction = new Transaction();

        transaction.setId(response.getId());
        transaction.setProductId(response.getProductId());

        if (response.getType() != null) {
            transaction.setType(Transaction.TransactionType.valueOf(response.getType().getValue()));
        }

        transaction.setAmount(response.getAmount());

        if (response.getDate() != null) {
            transaction.setDate(response.getDate().toLocalDateTime());
        }

        transaction.setDescription(response.getDescription().orElse(null));

        return transaction;
    }

    public static TransactionRequest toRequest(Transaction transaction) {
        if (transaction == null) return null;

        TransactionRequest request = new TransactionRequest(
                transaction.getProductId(),
                transaction.getType() != null
                        ? TransactionRequest.TypeEnum.fromValue(transaction.getType().name())
                        : null,
                transaction.getAmount(),
                transaction.getDate() != null
                        ? transaction.getDate().atOffset(OffsetDateTime.now().getOffset())
                        : null
        );

        if (transaction.getDescription() != null) {
            request.setDescription(JsonNullable.of(transaction.getDescription()));
        }

        return request;
    }
}