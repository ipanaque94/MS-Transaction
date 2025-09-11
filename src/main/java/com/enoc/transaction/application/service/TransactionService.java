package com.enoc.transaction.application.service;

import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    // Transacciones generales
    Mono<TransactionResponseDto> create(TransactionRequestDTO request);

    Mono<TransactionResponseDto> update(String id, TransactionRequestDTO request);

    Mono<TransactionResponseDto> deleteTransactionByLogicalState(String id);

    // Transacciones especializadas por tipo
    Mono<TransactionResponseDto> createDeposit(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createWithdrawal(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createCreditCharge(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createCreditPayment(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createInternalTransfer(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createExternalTransfer(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createDebitCardCharge(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createDebitCardPayment(TransactionRequestDTO request);

    Mono<TransactionResponseDto> createDebitWithdrawalOrdered(TransactionRequestDTO request);

    // Validaciones de negocio
    Mono<Boolean> hasOverdueCreditTransactions(String customerId);

    //Mono<Boolean> validateVipEligibility(String customerId);

    //Mono<Boolean> validatePymeEligibility(String customerId);

    // Cálculo de comisión
    Mono<Double> calculateTransactionFee(String accountId, TransactionType type);

    // Consultas por ID
    Mono<TransactionResponseDto> findById(String id);

    Mono<TransactionResponseDto> getTransactionById(String id);

    Mono<TransactionResponseDto> getActiveTransactionById(String id);

    Mono<TransactionResponseDto> getLastTransaction(String customerId);

    // Consultas por cliente o producto
    Flux<TransactionResponseDto> findAll();

    Flux<TransactionResponseDto> getTransactionsByCustomerId(String customerId);

    Flux<TransactionResponseDto> getTransactionsByProductId(String productId);

    Flux<TransactionResponseDto> getTransactionsByDateRange(OffsetDateTime start, OffsetDateTime end);

    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types);

    // Pagos y retiros con tarjeta
    Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO dto);

    Mono<TransactionResponseDto> processDebitCardPayment(TransactionRequestDTO dto);

    Mono<TransactionResponseDto> processOrderedDebitWithdrawal(TransactionRequestDTO dto);

    // Reportes especializados
    Mono<TransactionResponseDto> generateCustomerBalanceReport(String customerId, OffsetDateTime startDateTime, OffsetDateTime endDateTime);

    // Últimos movimientos
    //Flux<TransactionResponseDto> getLast10CardTransactions(String customerId);
}




