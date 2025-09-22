package com.enoc.transaction.domain.repository;


import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    // Custom method to count transactions by account ID and transaction type.
    // Método personalizado para contar transacciones por ID de cuenta y tipo de transacción.
    Mono<Long> countByAccountIdAndTypeIn(String accountId, List<String> typeNames);

    // Custom method to find a transaction by its ID and state.
    // Método personalizado para encontrar una transacción por su ID y estado.
    Mono<Transaction> findByIdAndState(String id, TransactionState state);

    // Method to check if there is an overdue credit transaction for a customer with a specific product.
    // Método para verificar si existe una transacción de crédito vencida para un cliente con un producto específico.
    Mono<Boolean> existsByCustomerIdAndTypeAndDateBeforeAndState(String customerId, TransactionType type, OffsetDateTime date,
                                                                 TransactionState state
    );

    // Custom method to get transactions by card ID and state, ordered by creation date.
    // Metodo personalizado para obtener transacciones por ID de tarjeta y estado, ordenadas por fecha de creación.
    Flux<Transaction> findByProductIdAndStateOrderByCreatedAtDesc(String productId, TransactionState state);

    // Custom method to get the last 10 transactions of a customer, ordered by creation date.
    // Método personalizado para obtener las últimas 10 transacciones de un cliente, ordenadas por fecha de creación.
    Flux<Transaction> findTop10ByCustomerIdAndStateOrderByCreatedAtDesc(String customerId, TransactionState state);

    // Method to get all transactions by a customer and transaction state.
    // Método para obtener todas las transacciones de un cliente y su estado de transacción.
    Flux<Transaction> findByCustomerIdAndState(String customerId, TransactionState state);

    // Method to get all transactions by product ID and transaction state.
    // Método para obtener todas las transacciones por ID de producto y estado de transacción.
    Flux<Transaction> findByProductIdAndState(String productId, TransactionState state);

    // Custom method to get the latest transaction for a customer, ordered by creation date.
    // Método personalizado para obtener la última transacción de un cliente, ordenada por fecha de creación.
    Mono<Transaction> findTopByCustomerIdAndStateOrderByCreatedAtDesc(String customerId, TransactionState state);

    // Method to get transactions created between two dates, filtered by transaction state.
    // Método para obtener transacciones creadas entre dos fechas, filtradas por estado de transacción.
    Flux<Transaction> findByCreatedAtBetweenAndState(OffsetDateTime start, OffsetDateTime end, TransactionState state);

    // Método para obtener transacciones por customerId, estado y rango de fechas
    Flux<Transaction> findByCustomerIdAndStateAndCreatedAtBetween(String customerId, TransactionState state, OffsetDateTime start,
                                                                  OffsetDateTime end);

}
