package com.enoc.transaction.mapper;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class TransactionMapper {

    public Transaction mapToEntity(TransactionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Transaction.builder()
                .id(UUID.randomUUID().toString())
                .customerId(dto.getCustomerId())
                .productId(dto.getProductId())
                .accountId(dto.getAccountId())
                .operationTypeId(dto.getOperationTypeId())
                .destinationAccountId(dto.getDestinationAccountId())
                .type(dto.getType())
                .origin(dto.getOrigin())
                .status(dto.getStatus())
                .amount(dto.getAmount())
                .commissionApplied(dto.getCommissionApplied())
                .date(dto.getDate())
                .eventDate(dto.getEventDate())
                .description(dto.getDescription())
                .build();
    }

    public TransactionResponseDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .customerId(transaction.getCustomerId())
                .productId(transaction.getProductId())
                .accountId(transaction.getAccountId())
                .operationTypeId(transaction.getOperationTypeId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .type(transaction.getType())
                .origin(transaction.getOrigin())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .commissionApplied(transaction.getCommissionApplied())
                .date(transaction.getDate())
                .eventDate(transaction.getEventDate())
                .description(transaction.getDescription())
                .build();
    }


}
