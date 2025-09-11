package com.enoc.transaction.infrastructure.service;

import com.enoc.transaction.application.service.TransactionService;
import com.enoc.transaction.domain.exception.BusinessException;
import com.enoc.transaction.domain.exception.ResourceNotFoundException;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.domain.service.TransactionValidator;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.mapper.TransactionMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    //@Autowired
    //private ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Value("${transaction.free-limit}")
    private int transactionLimit;

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final TransactionValidator validator;


    /*
     Method to create a transaction.
     Método para crear una transacción.
     */
    @Override
    public Mono<TransactionResponseDto> create(TransactionRequestDTO request) {
        switch (request.getType()) {
            case DEPOSIT:
                return createDeposit(request);
            case WITHDRAWAL:
                return createWithdrawal(request);
            case CREDIT_CHARGE:
                return createCreditCharge(request);
            case CREDIT_PAYMENT:
                return createCreditPayment(request);
            case TRANSFER_INTERNAL:
                return createInternalTransfer(request);
            case TRANSFER_EXTERNAL:
                return createExternalTransfer(request);
            case DEBIT_CARD_CHARGE:
                return createDebitCardCharge(request);
            case DEBIT_CARD_PAYMENT:
                return createDebitCardPayment(request);
            case DEBIT_WITHDRAWAL:
                return createDebitWithdrawalOrdered(request);
            default:
                return Mono.error(new IllegalArgumentException("Tipo de transacción no soportado"));
        }
    }

    @Override
    public Mono<TransactionResponseDto> createDeposit(TransactionRequestDTO request) {
        return validator.validarMontoPositivo(request)
                .then(hasOverdueCreditTransactions(request.getCustomerId()))
                .flatMap(hasDebt -> {
                    if (hasDebt) {
                        return Mono.error(new IllegalArgumentException("Cliente tiene deudas vencidas"));
                    }
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setType(TransactionType.DEPOSIT);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());
                    return repository.save(tx).map(mapper::toDto);
                });
    }


    @Override
    public Mono<TransactionResponseDto> createWithdrawal(TransactionRequestDTO request) {
        return validator.validarMontoPositivo(request)
                .then(hasOverdueCreditTransactions(request.getCustomerId()))
                .flatMap(hasDebt -> {
                    if (hasDebt) {
                        return Mono.error(new IllegalArgumentException("Cliente tiene deudas vencidas"));
                    }
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setType(TransactionType.WITHDRAWAL);
                    tx.setAmount(request.getAmount().negate());
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());
                    return repository.save(tx).map(mapper::toDto);
                });
    }


    @Override
    public Mono<TransactionResponseDto> createCreditCharge(TransactionRequestDTO request) {
        Transaction tx = mapper.mapToEntity(request);
        tx.setType(TransactionType.CREDIT_CHARGE);
        tx.setState(TransactionState.ACTIVE);
        tx.setCreatedAt(OffsetDateTime.now());
        return repository.save(tx).map(mapper::toDto);
    }


    @Override
    public Mono<TransactionResponseDto> createCreditPayment(TransactionRequestDTO request) {
        return hasOverdueCreditTransactions(request.getCustomerId())
                .flatMap(hasDebt -> {
                    if (!hasDebt) {
                        return Mono.error(new IllegalArgumentException("No hay deuda vencida para pagar"));
                    }
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setType(TransactionType.CREDIT_PAYMENT);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());
                    return repository.save(tx).map(mapper::toDto);
                });
    }

    /* con circuit
    @Override
public Mono<TransactionResponseDto> createCreditPayment(TransactionRequestDTO request) {
    return circuitBreakerFactory.create("customerCircuitBreaker")
        .run(
            customerClient.validatePaymentAuthorization(request.getCustomerId(), request.getProductId()),
            throwable -> Mono.just(false)
        )
        .flatMap(isAuthorized -> {
            if (!isAuthorized) {
                return Mono.error(new BusinessException("Cliente no autorizado para pagar este producto"));
            }
            Transaction tx = mapper.mapToEntity(request);
            tx.setType(TransactionType.CREDIT_PAYMENT);
            tx.setState(TransactionState.ACTIVE);
            tx.setCreatedAt(OffsetDateTime.now());
            return repository.save(tx).map(mapper::toDto);
        });
}
*/

    @Override
    public Mono<TransactionResponseDto> createInternalTransfer(TransactionRequestDTO request) {
        Transaction tx = mapper.mapToEntity(request);
        tx.setType(TransactionType.TRANSFER_INTERNAL);
        tx.setState(TransactionState.ACTIVE);
        tx.setCreatedAt(OffsetDateTime.now());
        return repository.save(tx).map(mapper::toDto);
    }


    @Override
    public Mono<TransactionResponseDto> createExternalTransfer(TransactionRequestDTO request) {
        Transaction tx = mapper.mapToEntity(request);
        tx.setType(TransactionType.TRANSFER_EXTERNAL);
        tx.setState(TransactionState.ACTIVE);
        tx.setCreatedAt(OffsetDateTime.now());
        return repository.save(tx).map(mapper::toDto);
    }
/*
    @Override
    public Mono<TransactionResponseDto> createExternalTransfer(TransactionRequestDTO request) {
        return circuitBreakerFactory.create("productCircuitBreaker")
                .run(
                        productClient.getProductById(request.getProductId()),
                        throwable -> Mono.error(new BusinessException("product-service no respondió"))
                )
                .flatMap(product -> {
                    if (!product.isActive()) {
                        return Mono.error(new BusinessException("Producto destino inactivo"));
                    }
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setType(TransactionType.TRANSFER_EXTERNAL);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());
                    return repository.save(tx).map(mapper::toDto);
                });
    }
*/

    @Override
    public Mono<TransactionResponseDto> createDebitCardCharge(TransactionRequestDTO request) {
        Transaction tx = mapper.mapToEntity(request);
        tx.setType(TransactionType.DEBIT_CARD_CHARGE);
        tx.setOrigin(TransactionOrigin.DEBIT_CARD);
        tx.setState(TransactionState.ACTIVE);
        tx.setCreatedAt(OffsetDateTime.now());
        return repository.save(tx).map(mapper::toDto);
    }


    @Override
    public Mono<TransactionResponseDto> createDebitCardPayment(TransactionRequestDTO request) {
        return Mono.when(
                validator.validarMontoPositivo(request),
                validator.validarMontoMaximo(request, new BigDecimal("10000"))
        ).then(Mono.defer(() -> {
            Transaction tx = mapper.mapToEntity(request);
            tx.setType(TransactionType.DEBIT_CARD_PAYMENT);
            tx.setOrigin(TransactionOrigin.DEBIT_CARD);
            tx.setState(TransactionState.ACTIVE);
            tx.setCreatedAt(OffsetDateTime.now());
            return repository.save(tx).map(mapper::toDto);
        }));
    }


    @Override
    public Mono<TransactionResponseDto> createDebitWithdrawalOrdered(TransactionRequestDTO request) {
        return repository.findByCardIdAndStateOrderByCreatedAtDesc(request.getCardId(), TransactionState.ACTIVE)
                .collectList()
                .flatMap(transactions -> {
                    // Agrupar por accountId y calcular saldo acumulado
                    Map<String, BigDecimal> saldos = new LinkedHashMap<>();
                    for (Transaction tx : transactions) {
                        saldos.merge(tx.getAccountId(), tx.getAmount(), BigDecimal::add);
                    }

                    // Buscar la primera cuenta con saldo suficiente
                    Optional<Map.Entry<String, BigDecimal>> cuentaValida = saldos.entrySet().stream()
                            .filter(entry -> entry.getValue().compareTo(request.getAmount()) >= 0)
                            .findFirst();

                    if (cuentaValida.isEmpty()) {
                        return Mono.error(new BusinessException("Saldo insuficiente en cuentas asociadas"));
                    }

                    // Crear la transacción de retiro
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setAccountId(cuentaValida.get().getKey());
                    tx.setAmount(request.getAmount().negate());
                    tx.setType(TransactionType.DEBIT_WITHDRAWAL);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());

                    return repository.save(tx).map(mapper::toDto);
                });
    }


    /*
      Method to get all transactions.
      Método para obtener todas las transacciones.
     */
    @Override
    public Flux<TransactionResponseDto> findAll() {
        return repository.findAll()
                .map(mapper::toDto);
    }

    /*
     Method to get a transaction by its ID.
     Método para obtener una transacción por su ID.
     */
    @Override
    public Mono<TransactionResponseDto> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    /*
     Method to update a transaction.
     Método para actualizar una transacción.
     */
    @Override
    public Mono<TransactionResponseDto> update(String id, TransactionRequestDTO request) {
        return hasOverdueCreditTransactions(request.getCustomerId())
                .flatMap(hasDebt -> {
                    if (hasDebt) {
                        return Mono.error(new IllegalArgumentException("Cliente tiene deudas vencidas"));
                    }
                    // Si no tiene deuda vencida, proceder con la actualización
                    return repository.findById(id)
                            .flatMap(existing -> {
                                existing.setAmount(request.getAmount());
                                existing.setType(TransactionType.TRANSFER_INTERNAL);
                                existing.setDate(request.getDate());
                                existing.setDescription(request.getDescription());
                                return repository.save(existing);
                            })
                            .map(mapper::toDto);
                });
    }

    /*
     Method to count transactions by account and type.
     Método para contar transacciones por cuenta y tipo.
     */
    @Override
    public Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types) {
        List<String> typeNames = types.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return repository.countByAccountIdAndTypeIn(accountId, typeNames);
    }

    /*
      Method to check if a customer has overdue credit transactions.
      Método para verificar si un cliente tiene transacciones de crédito vencidas.
     */
    @Override
    public Mono<Boolean> hasOverdueCreditTransactions(String customerId) {
        return repository.existsByCustomerIdAndTypeAndDateBeforeAndState(
                customerId,
                TransactionType.CREDIT_CHARGE,
                OffsetDateTime.now(),
                TransactionState.ACTIVE
        );
    }
/*
    @Override
    public Mono<Boolean> validateVipEligibility(String customerId) {
        return repository.existsByCustomerIdAndTypeAndDateBeforeAndState(
                customerId,
                TransactionType.CREDIT_CHARGE,
                OffsetDateTime.now(),
                TransactionState.ACTIVE
        ).map(hasDebt -> !hasDebt); // Debe no tener deuda
    }

    @Override
    public Mono<Boolean> validatePymeEligibility(String customerId) {
        return repository.existsByCustomerIdAndTypeAndDateBeforeAndState(
                customerId,
                TransactionType.CREDIT_CHARGE,
                OffsetDateTime.now(),
                TransactionState.ACTIVE
        ).map(hasDebt -> !hasDebt); // Debe no tener deuda
    }
*/

    /*
      Method to pay a third-party credit product.
      Método para pagar un producto de crédito de terceros.
     */
    @Override
    public Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO dto) {
        return hasOverdueCreditTransactions(dto.getDebtorDni())
                .flatMap(hasDebt -> {
                    if (hasDebt) {
                        Transaction transaction = mapper.mapToEntity(dto);
                        transaction.setType(TransactionType.CREDIT_PAYMENT);
                        transaction.setOrigin(TransactionOrigin.CREDIT);
                        transaction.setState(TransactionState.ACTIVE);
                        transaction.setCreatedAt(OffsetDateTime.now());

                        return repository.save(transaction)
                                .map(mapper::toDto);
                    } else {
                        return Mono.error(new IllegalArgumentException("Debt not found or insufficient to make payment"));
                    }
                });
    }

    /*
        @Override
        public Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO request) {
            return circuitBreakerFactory.create("customerCircuitBreaker")
                    .run(
                            customerClient.validateThirdPartyPayment(request.getCustomerId(), request.getProductId()),
                            throwable -> Mono.just(false)
                    )
                    .flatMap(isAuthorized -> {
                        if (!isAuthorized) {
                            return Mono.error(new BusinessException("Cliente no autorizado para pagar producto de terceros"));
                        }
                        Transaction tx = mapper.mapToEntity(request);
                        tx.setType(TransactionType.CREDIT_PAYMENT);
                        tx.setState(TransactionState.ACTIVE);
                        tx.setCreatedAt(OffsetDateTime.now());
                        return repository.save(tx).map(mapper::toDto);
                    });
        }
    */
    /*
      Method to process payment with debit card.
      Método para procesar el pago con tarjeta de débito.
     */
    @Override
    public Mono<TransactionResponseDto> processDebitCardPayment(TransactionRequestDTO dto) {
        Transaction tx = mapper.mapToEntity(dto);
        tx.setType(TransactionType.PAYMENT);
        tx.setOrigin(TransactionOrigin.DEBIT_CARD);
        tx.setState(TransactionState.ACTIVE);
        tx.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return repository.save(tx)
                .map(mapper::toDto);
    }
    /*
    @Override
    public Mono<TransactionResponseDto> processDebitCardPayment(TransactionRequestDTO request) {
        return circuitBreakerFactory.create("productCircuitBreaker")
                .run(
                        productClient.getDebitCardDetails(request.getCardId()),
                        throwable -> Mono.error(new BusinessException("Error al consultar tarjeta"))
                )
                .flatMap(card -> {
                    if (!card.isActive()) {
                        return Mono.error(new BusinessException("Tarjeta inactiva"));
                    }
                    // Validar cuentas asociadas, saldo, etc.
                    Transaction tx = mapper.mapToEntity(request);
                    tx.setType(TransactionType.DEBIT_CARD_PAYMENT);
                    tx.setOrigin(TransactionOrigin.DEBIT_CARD);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now());
                    return repository.save(tx).map(mapper::toDto);
                });
    }
    */

    /*
      Method to process debit card withdrawal according to the order of accounts.
      Método para procesar un retiro con tarjeta de débito según el orden de cuentas.
     */
    @Override
    public Mono<TransactionResponseDto> processOrderedDebitWithdrawal(TransactionRequestDTO dto) {
        return repository.findByCardIdAndStateOrderByCreatedAtDesc(dto.getCardId(), TransactionState.ACTIVE)
                .next()
                .flatMap(transaction -> {
                    BigDecimal availableAmount = transaction.getAmount();

                    if (availableAmount.compareTo(dto.getAmount()) < 0) {
                        return Mono.error(new BusinessException("Insufficient funds"));
                    }

                    // Crea la transacción de retiro
                    Transaction tx = mapper.mapToEntity(dto);
                    tx.setAmount(dto.getAmount().negate()); // El monto se resta de la cuenta
                    tx.setType(TransactionType.DEBIT_WITHDRAWAL);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

                    // Guarda la nueva transacción de retiro
                    return repository.save(tx)
                            .map(mapper::toDto); // Devuelve la transacción como DTO
                });
    }
/*
    @Override
    public Mono<TransactionResponseDto> processOrderedDebitWithdrawal(TransactionRequestDTO request) {
        return circuitBreakerFactory.create("productCircuitBreaker")
                .run(
                        productClient.getOrderedAccountsByCardId(request.getCardId()),
                        throwable -> Mono.error(new BusinessException("No se pudo consultar cuentas asociadas"))
                )
                .flatMap(accounts -> {
                    for (Account account : accounts) {
                        if (account.getBalance().compareTo(request.getAmount()) >= 0) {
                            Transaction tx = mapper.mapToEntity(request);
                            tx.setAccountId(account.getId());
                            tx.setAmount(request.getAmount().negate());
                            tx.setType(TransactionType.DEBIT_WITHDRAWAL);
                            tx.setState(TransactionState.ACTIVE);
                            tx.setCreatedAt(OffsetDateTime.now());
                            return repository.save(tx).map(mapper::toDto);
                        }
                    }
                    return Mono.error(new BusinessException("Saldo insuficiente en cuentas asociadas"));
                });
    }

   */


    /*
      Method to get the active transaction by ID.
      Método para obtener la transacción activa por ID.
     */
    @Override
    public Mono<TransactionResponseDto> getActiveTransactionById(String id) {
        return repository.findByIdAndState(id, TransactionState.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Active transaction not found")))
                .map(mapper::toDto);
    }

    /*
      Method to get a transaction by its ID.
      Método para obtener una transacción por su ID.
     */
    @Override
    public Mono<TransactionResponseDto> getTransactionById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Transaction not found")))
                .map(mapper::toDto);
    }

    /*
      Method to delete transactions logically (if necessary).
      Método para eliminar transacciones de manera lógica (si fuera necesario).
     */
    @Override
    public Mono<TransactionResponseDto> deleteTransactionByLogicalState(String id) {
        return repository.findByIdAndState(id, TransactionState.ACTIVE)
                .switchIfEmpty(Mono.error(new NotFoundException("Transaction not found or already deleted")))
                .flatMap(tx -> {
                    tx.setState(TransactionState.INACTIVE);
                    return repository.save(tx);
                })
                .map(mapper::toDto);
    }


    /*
      Method to get transactions by customer ID.
      Método para obtener las transacciones de un cliente.
     */
    @Override
    public Flux<TransactionResponseDto> getTransactionsByCustomerId(String customerId) {
        return repository.findByCustomerIdAndState(customerId, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }

    /*
      Method to get transactions by product ID.
      Método para obtener las transacciones de un producto específico.
     */
    @Override
    public Flux<TransactionResponseDto> getTransactionsByProductId(String productId) {
        return repository.findByProductIdAndState(productId, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }

    /*
      Method to get transactions within a date range.
      Método para obtener las transacciones dentro de un rango de fechas.
     */
    @Override
    public Flux<TransactionResponseDto> getTransactionsByDateRange(OffsetDateTime start, OffsetDateTime end) {
        return repository.findByCreatedAtBetweenAndState(start, end, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }


    /*
      Method to get the last transaction of a customer.
      Método para obtener la última transacción de un cliente.
     */
    @Override
    public Mono<TransactionResponseDto> getLastTransaction(String customerId) {
        return repository.findTopByCustomerIdAndStateOrderByCreatedAtDesc(customerId, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }

    /*
      Method to calculate transaction fee (if the limit of free transactions is exceeded).
      Método para calcular la comisión de transacciones (si el límite de transacciones sin comisión es superado).
     */
    @Override
    public Mono<Double> calculateTransactionFee(String accountId, TransactionType type) {
        return repository.countByAccountIdAndTypeIn(accountId, List.of(type.name()))
                .flatMap(transactionCount -> {
                    double commission = 0.0;
                    if (transactionCount > transactionLimit) {
                        commission = 5.0; // Comisión fija
                    }
                    return Mono.just(commission);
                });
    }

    /*
      Method to generate a consolidated balance report for a customer over a specified period.
      Método para generar un reporte consolidado de saldo promedio diario de un cliente en un periodo determinado.
     */
    @Override
    public Mono<TransactionResponseDto> generateCustomerBalanceReport(String customerId, OffsetDateTime startDateTime,
                                                                      OffsetDateTime endDateTime) {
        return repository.findByCustomerIdAndStateAndCreatedAtBetween(customerId, TransactionState.ACTIVE, startDateTime, endDateTime)
                .collectList() // Convertir Flux a lista
                .flatMap(transactions -> {
                    BigDecimal totalAmount = transactions.stream()
                            .map(Transaction::getAmount) // Obtener el monto de cada transacción
                            .reduce(BigDecimal.ZERO, BigDecimal::add); // Sumar los montos de las transacciones

                    // Calcular el saldo promedio
                    double averageBalance = 0.0;
                    if (!transactions.isEmpty()) {
                        averageBalance = totalAmount.doubleValue() / transactions.size(); // Dividir entre el número de transacciones
                    }
                    // Crear el DTO para el reporte
                    TransactionResponseDto report = new TransactionResponseDto();
                    report.setAverageBalance(averageBalance); // Establecer el saldo promedio
                    report.setTotalAmount(totalAmount.doubleValue()); // Establecer el total de las transacciones
                    return Mono.just(report); // Retornar el reporte
                });
    }


}