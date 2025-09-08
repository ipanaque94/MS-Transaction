package com.enoc.transaction.service;

import com.enoc.transaction.application.internal.TransactionDomainService;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.domain.service.TransactionValidator;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.persistence.TransactionRepository;
import com.enoc.transaction.infrastructure.service.TransactionServiceImpl;
import com.enoc.transaction.mapper.TransactionMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private TransactionServiceImpl service;

    @Mock
    private TransactionValidator validator;

    @Mock
    private TransactionDomainService transactionDomainService;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(repository, mapper, validator, transactionDomainService);
    }


    @Test
    void createShouldApplyCommissionWhenLimitExceeded() {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .customerId("cust123")
                .productId("prod456")
                .accountId("acc789")
                .operationTypeId("op001")
                .destinationAccountId("dest002")
                .type(TransactionType.DEPOSIT)
                .origin(TransactionOrigin.DEBIT_CARD)
                .status(StatusEnum.PENDING)
                .amount(new BigDecimal("100.00"))
                .commissionApplied(BigDecimal.ZERO)
                .date(OffsetDateTime.now())
                .eventDate(OffsetDateTime.now())
                .description("Test deposit")
                .build();

        Transaction entity = new Transaction();
        entity.setAccountId("acc789");
        entity.setAmount(new BigDecimal("100.00"));
        entity.setType(TransactionType.DEPOSIT);
        entity.setCommissionApplied(new BigDecimal("2.50"));

        TransactionResponseDto responseDto = TransactionResponseDto.builder()
                .accountId("acc789")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .commissionApplied(new BigDecimal("2.50"))
                .description("Test deposit")
                .build();

        // Act
        when(repository.countByAccountIdAndTypeIn(anyString(), anyList())).thenReturn(Mono.just(6L));
        when(mapper.mapToEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(validator.validarMontoPositivo(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.empty());
        when(mapper.toDto(entity)).thenReturn(responseDto);

        // Assert
        StepVerifier.create(service.create(request))
                .expectNextMatches(dto -> dto.getCommissionApplied().compareTo(new BigDecimal("2.50")) == 0)
                .verifyComplete();

    }

    @Test
    void findAllShouldReturnMappedTransactions() {
        Transaction t1 = new Transaction();
        t1.setAccountId("acc001");
        t1.setAmount(new BigDecimal("50.00"));
        t1.setType(TransactionType.PAYMENT);

        TransactionResponseDto dto1 = TransactionResponseDto.builder()
                .accountId("acc001")
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.PAYMENT)
                .build();

        when(repository.findAll()).thenReturn(Flux.just(t1));
        when(mapper.toDto(t1)).thenReturn(dto1);

        StepVerifier.create(service.findAll())
                .expectNext(dto1)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnDtoIfExists() {
        Transaction t = new Transaction();
        t.setAccountId("acc002");
        t.setAmount(new BigDecimal("75.00"));
        t.setType(TransactionType.CREDIT_CHARGE);

        TransactionResponseDto dto = TransactionResponseDto.builder()
                .accountId("acc002")
                .amount(new BigDecimal("75.00"))
                .type(TransactionType.CREDIT_CHARGE)
                .build();

        when(repository.findById("123")).thenReturn(Mono.just(t));
        when(mapper.toDto(t)).thenReturn(dto);

        StepVerifier.create(service.findById("123"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void updateShouldModifyAndReturnUpdatedTransaction() {
        Transaction existing = new Transaction();
        existing.setId("123");
        existing.setAccountId("acc003");
        existing.setAmount(new BigDecimal("20.00"));
        existing.setType(TransactionType.DEPOSIT);

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .amount(new BigDecimal("30.00"))
                .date(OffsetDateTime.now())
                .description("Updated transaction")
                .build();

        TransactionResponseDto dto = TransactionResponseDto.builder()
                .accountId("acc003")
                .amount(new BigDecimal("30.00"))
                .type(TransactionType.TRANSFER_INTERNAL)
                .description("Updated transaction")
                .build();

        when(repository.findById("123")).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(existing));
        when(mapper.toDto(existing)).thenReturn(dto);

        StepVerifier.create(service.update("123", request))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void deleteShouldCompleteSuccessfully() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId("123");
        transaction.setState(TransactionState.ACTIVE);

        Transaction updated = new Transaction();
        updated.setId("123");
        updated.setState(TransactionState.INACTIVE);

        when(repository.findByIdAndState("123", TransactionState.ACTIVE)).thenReturn(Mono.just(transaction));
        when(repository.save(any(Transaction.class))).thenReturn(Mono.just(updated));
        when(mapper.toDto(updated)).thenReturn(new TransactionResponseDto()); // puedes mockear campos si lo deseas

        StepVerifier.create(service.deleteTransactionByLogicalState("123"))
                .expectNextMatches(dto -> dto != null) // o validar campos específicos
                .verifyComplete();
    }

    @Test
    void countByAccountIdAndTypeInShouldReturnCount() {
        when(repository.countByAccountIdAndTypeIn("acc123", List.of("DEPOSIT", "WITHDRAWAL")))
                .thenReturn(Mono.just(3L));

        StepVerifier.create(service.countByAccountIdAndTypeIn("acc123", List.of(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL)))
                .expectNext(3L)
                .verifyComplete();
    }


}
