package com.enoc.transaction.application.service;

import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.time.LocalDate;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Mono<TransactionResponseDto> create(TransactionRequestDTO request);

    Flux<TransactionResponseDto> findAll();

    Mono<TransactionResponseDto> findById(String id);

    Mono<TransactionResponseDto> update(String id, TransactionRequestDTO request);

    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types);

    Mono<Boolean> hasOverdueCreditTransactions(String customerId);

    Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO dto);

    Mono<TransactionResponseDto> processDebitCardPayment(TransactionRequestDTO dto);

    Mono<List<TransactionResponseDto>> processOrderedDebitWithdrawal(TransactionRequestDTO dto);

    Flux<TransactionResponseDto> getLast10CardTransactions(String customerId);

    Mono<TransactionResponseDto> getActiveTransactionById(String id);

    Mono<TransactionResponseDto> getTransactionById(String id);

    Mono<TransactionResponseDto> deleteTransactionByLogicalState(String id);

    Flux<TransactionResponseDto> getTransactionsByCustomerId(String customerId);

    Flux<TransactionResponseDto> getTransactionsByProductId(String productId);

    Flux<TransactionResponseDto> getTransactionsByDateRange(LocalDate start, LocalDate end);

    Mono<TransactionResponseDto> getLastTransaction(String customerId);


}




