package com.enoc.transaction.infrastructure.service;

import com.enoc.transaction.application.internal.TransactionDomainService;
import com.enoc.transaction.application.service.TransactionService;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.domain.service.TransactionValidator;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.exception.ResourceNotFoundException;
import com.enoc.transaction.exception.business.BusinessException;
import com.enoc.transaction.infrastructure.persistence.TransactionRepository;
import com.enoc.transaction.mapper.TransactionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final TransactionValidator validator;
    private final TransactionDomainService transactionDomainService;

    private static final int FREE_TRANSACTION_LIMIT = 5;
    private static final BigDecimal COMMISSION_AMOUNT = new BigDecimal("2.50");

    @Override
    public Mono<TransactionResponseDto> create(TransactionRequestDTO request) {
        boolean isCommissionable = request.getType().isCommissionable();

        return validator.validarMontoPositivo(request)
                .flatMap(valid -> repository.countByAccountIdAndTypeIn(
                                        request.getAccountId(),
                                        List.of(request.getType().name()) // Solo cuenta si es del tipo actual
                                )
                                .flatMap(count -> {
                                    Transaction entity = mapper.mapToEntity(request);

                                    entity.setCommissionApplied(
                                            isCommissionable && count >= FREE_TRANSACTION_LIMIT
                                                    ? COMMISSION_AMOUNT
                                                    : BigDecimal.ZERO
                                    );

                                    return repository.save(entity);
                                })
                )
                .map(mapper::toDto);

    }


    @Override
    public Flux<TransactionResponseDto> findAll() {
        return repository.findAll()
                .map(mapper::toDto);
    }

    @Override
    public Mono<TransactionResponseDto> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    public Mono<TransactionResponseDto> update(String id, TransactionRequestDTO request) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setAmount(request.getAmount());
                    existing.setType(TransactionType.TRANSFER_INTERNAL);
                    existing.setDate(request.getDate());
                    existing.setDescription(request.getDescription());
                    return repository.save(existing);
                })
                .map(mapper::toDto);
    }

    //Contar cuántas transacciones existen para una cuenta específica
    @Override
    public Mono<Long> countByAccountIdAndTypeIn(String accountId, List<TransactionType> types) {
        List<String> typeNames = types.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return repository.countByAccountIdAndTypeIn(accountId, typeNames);
    }

    //Verifica si un cliente tiene transacciones de crédito vencidas activas
    @Override
    public Mono<Boolean> hasOverdueCreditTransactions(String customerId) {
        return repository.existsByCustomerIdAndTypeAndDueDateBeforeAndState(
                customerId,
                TransactionType.CREDIT_CHARGE,
                LocalDate.now(),
                TransactionState.ACTIVE
        );

    }

    // pago de producto de crédito de un tercero
    @Override
    public Mono<TransactionResponseDto> payThirdPartyCreditProduct(TransactionRequestDTO dto) {
        return transactionDomainService.validarDeudaExistente(dto.getDebtorDni(), dto.getAmount())
                .flatMap(debt -> transactionDomainService.registrar(dto)
                        .flatMap(tx -> transactionDomainService.actualizarDeuda(debt, dto.getAmount())
                                .thenReturn(mapper.toDto(tx))
                        )
                );
    }


    // procesar Un pago con la tarjeta de debito
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

    @Override
    public Mono<List<TransactionResponseDto>> processOrderedDebitWithdrawal(TransactionRequestDTO dto) {
        return repository.findByCardIdAndStateOrderByCreatedAtDesc(dto.getCardId(), TransactionState.ACTIVE)
                .collectList()
                .flatMap(transactions -> {
                    BigDecimal total = transactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    if (total.compareTo(dto.getAmount()) < 0) {
                        return Mono.error(new BusinessException("Insufficient funds"));
                    }

                    Transaction tx = mapper.mapToEntity(dto); // ← construcción profesional
                    tx.setAmount(dto.getAmount().negate());
                    tx.setType(TransactionType.DEBIT_WITHDRAWAL);
                    tx.setState(TransactionState.ACTIVE);
                    tx.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

                    return repository.save(tx)
                            .map(mapper::toDto)
                            .map(List::of);
                });
    }


    @Override
    public Flux<TransactionResponseDto> getLast10CardTransactions(String customerId) {
        return repository.findTop10ByCustomerIdAndStateOrderByCreatedAtDesc(customerId, TransactionState.ACTIVE)
                .map(mapper::toDto);

    }


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

    @Override
    public Flux<TransactionResponseDto> getTransactionsByCustomerId(String customerId) {
        return repository.findByCustomerIdAndState(customerId, TransactionState.ACTIVE)
                .map(mapper::toDto);

    }

    @Override
    public Flux<TransactionResponseDto> getTransactionsByProductId(String productId) {
        return repository.findByProductIdAndState(productId, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }

    @Override
    public Flux<TransactionResponseDto> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        OffsetDateTime startDateTime = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = end.atTime(23, 59).atOffset(ZoneOffset.UTC);

        return repository.findByCreatedAtBetweenAndState(startDateTime, endDateTime, TransactionState.ACTIVE)
                .map(mapper::toDto);
    }


    @Override
    public Mono<TransactionResponseDto> getLastTransaction(String customerId) {
        return repository.findTopByCustomerIdAndStateOrderByCreatedAtDesc(customerId, TransactionState.ACTIVE)
                .map(mapper::toDto);

    }

    @Override
    public Mono<TransactionResponseDto> getActiveTransactionById(String id) {
        return repository.findByIdAndState(id, TransactionState.ACTIVE)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Active transaction not found")))
                .map(mapper::toDto);
    }

    @Override
    public Mono<TransactionResponseDto> getTransactionById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Transaction not found")))
                .map(mapper::toDto);


    }


}
