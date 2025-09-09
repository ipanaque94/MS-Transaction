package com.enoc.transaction.application.service;

import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    // Method to create a transaction
    // Método para crear una transacción
    Mono<TransactionResponseDto> create(TransactionRequestDTO request);

    // Method to get all transactions
    // Método para obtener todas las transacciones
    Flux<TransactionResponseDto> findAll();

    // Method to get a transaction by its ID
    // Método para obtener una transacción por su ID
    Mono<TransactionResponseDto> findById(String id);

    // Method to update a transaction
    // Método para actualizar una transacción
    Mono<TransactionResponseDto> update(String id, TransactionRequestDTO request);

    // Method to count transactions by account and type
    // Método para contar transacciones por cuenta y tipo
    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types);

    // Method to check if a customer has overdue credit transactions
    // Método para verificar si un cliente tiene transacciones de crédito vencidas
    Mono<Boolean> hasOverdueCreditTransactions(String customerId);

    // Method to pay a third-party credit product
    // Método para pagar un producto de crédito de terceros
    Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO dto);

    // Method to process payment with debit card
    // Método para procesar el pago con tarjeta de débito
    Mono<TransactionResponseDto> processDebitCardPayment(TransactionRequestDTO dto);

    // Method to process a debit card withdrawal according to the order of accounts
    // Método para procesar un retiro con tarjeta de débito de acuerdo con el orden de las cuentas
    Mono<TransactionResponseDto> processOrderedDebitWithdrawal(TransactionRequestDTO dto);


    // Method to get the last 10 debit card transactions
    // Método para obtener los últimos 10 movimientos de la tarjeta de débito
    Flux<TransactionResponseDto> getLast10CardTransactions(String customerId);

    // Method to get the active transaction by ID
    // Método para obtener la transacción activa por ID
    Mono<TransactionResponseDto> getActiveTransactionById(String id);

    // Method to get a transaction by its ID
    // Método para obtener una transacción por su ID
    Mono<TransactionResponseDto> getTransactionById(String id);

    // Method to delete transactions logically (if necessary)
    // Método para eliminar transacciones de manera lógica (si fuera necesario)
    Mono<TransactionResponseDto> deleteTransactionByLogicalState(String id);

    // Method to get transactions by customer ID
    // Método para obtener las transacciones de un cliente
    Flux<TransactionResponseDto> getTransactionsByCustomerId(String customerId);

    // Method to get transactions by product ID
    // Método para obtener las transacciones de un producto específico
    Flux<TransactionResponseDto> getTransactionsByProductId(String productId);

    // Method to get transactions within a date range
    // Método para obtener las transacciones dentro de un rango de fechas
    public Flux<TransactionResponseDto> getTransactionsByDateRange(OffsetDateTime start, OffsetDateTime end);

    // Method to get the last transaction of a customer
    // Método para obtener la última transacción de un cliente
    Mono<TransactionResponseDto> getLastTransaction(String customerId);

    // Method to calculate transaction fee (if the limit of free transactions is exceeded)
    // Método para calcular la comisión de transacciones (si el límite de transacciones sin comisión es superado)
    Mono<Double> calculateTransactionFee(String accountId, TransactionType type);

    // Method to generate a consolidated balance report for a customer over a specified period
    // Método para generar el reporte consolidado con OffsetDateTime en lugar de LocalDate
    Mono<TransactionResponseDto> generateCustomerBalanceReport(String customerId, OffsetDateTime startDateTime, OffsetDateTime endDateTime);
}



