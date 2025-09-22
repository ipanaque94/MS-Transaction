package com.enoc.transaction.service;

import com.enoc.transaction.application.service.cache.ReactiveCachedTransactionService;
import com.enoc.transaction.domain.exception.ResourceNotFoundException;
import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.domain.service.TransactionValidator;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.events.ExternalTransferRequested;
import com.enoc.transaction.infrastructure.mapper.TransactionMapper;
import com.enoc.transaction.infrastructure.messaging.producer.ExternalTransferProducer;
import com.enoc.transaction.infrastructure.service.TransactionServiceImpl;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;
    @Mock
    private ReactiveCachedTransactionService cachedService;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private TransactionValidator validator;

    @Mock
    private ExternalTransferProducer externalTransferProducer;

    @InjectMocks
    private TransactionServiceImpl service;

    private TransactionRequestDTO requestDto;

    @Test
    void createShouldDelegateToCreateDeposit() {
        // Arrange
        TransactionRequestDTO requestDto = TransactionRequestDTO.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .customerId("cust123")
                .accountId("acc789")
                .description("Test deposit")
                .build();

        Transaction transaction = new Transaction();
        transaction.setId("tx001");
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setAccountId("acc789");

        TransactionResponseDto expectedDto = TransactionResponseDto.builder()
                .id("tx001")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .accountId("acc789")
                .description("Test deposit")
                .build();

        // Stubbing solo lo necesario
        when(validator.validarMontoPositivo(any())).thenReturn(Mono.empty());
        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(
                eq("cust123"),
                eq(TransactionType.CREDIT_CHARGE),
                any(OffsetDateTime.class),
                eq(TransactionState.ACTIVE)
        )).thenReturn(Mono.just(false));
        when(mapper.mapToEntity(any())).thenReturn(transaction);
        when(repository.save(any())).thenReturn(Mono.just(transaction));
        when(mapper.toDto(any())).thenReturn(expectedDto);

        // Act & Assert
        StepVerifier.create(service.create(requestDto))
                .expectNextMatches(dto ->
                        dto.getId().equals("tx001") &&
                                dto.getType() == TransactionType.DEPOSIT &&
                                dto.getAmount().compareTo(new BigDecimal("100.00")) == 0 &&
                                dto.getAccountId().equals("acc789")
                )
                .verifyComplete();
    }

    private TransactionRequestDTO buildRequest(TransactionType type) {
        return TransactionRequestDTO.builder()
                .type(type)
                .amount(new BigDecimal("100.00"))
                .customerId("cust123")
                .accountId("acc789")
                .description("Test " + type.name())
                .date(OffsetDateTime.now())
                .eventDate(OffsetDateTime.now())
                .build();
    }

    private Transaction buildTransaction(TransactionType type) {
        Transaction tx = new Transaction();
        tx.setId("tx001");
        tx.setType(type);
        tx.setAmount(new BigDecimal("100.00"));
        tx.setAccountId("acc789");
        tx.setCustomerId("cust123");
        tx.setCreatedAt(OffsetDateTime.now());
        tx.setState(TransactionState.ACTIVE);
        tx.setDescription("Test " + type.name());
        return tx;
    }

    private TransactionResponseDto buildResponseDto(TransactionType type) {
        return TransactionResponseDto.builder()
                .id("tx001")
                .type(type)
                .amount(new BigDecimal("100.00"))
                .accountId("acc789")
                .customerId("cust123")
                .description("Test " + type.name())
                .build();
    }

    @Test
    void createDepositShouldSucceedWhenNoOverdueDebt() {
        TransactionRequestDTO request = buildRequest(TransactionType.DEPOSIT);
        Transaction tx = buildTransaction(TransactionType.DEPOSIT);
        TransactionResponseDto expected = buildResponseDto(TransactionType.DEPOSIT);

        when(validator.validarMontoPositivo(any())).thenReturn(Mono.empty());
        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createDeposit(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createWithdrawalShouldSucceedWhenNoOverdueDebt() {
        TransactionRequestDTO request = buildRequest(TransactionType.WITHDRAWAL);
        Transaction tx = buildTransaction(TransactionType.WITHDRAWAL);
        tx.setAmount(request.getAmount().negate());
        TransactionResponseDto expected = buildResponseDto(TransactionType.WITHDRAWAL);

        when(validator.validarMontoPositivo(any())).thenReturn(Mono.empty());
        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createWithdrawal(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createCreditChargeShouldPersistTransaction() {
        TransactionRequestDTO request = buildRequest(TransactionType.CREDIT_CHARGE);
        Transaction tx = buildTransaction(TransactionType.CREDIT_CHARGE);
        TransactionResponseDto expected = buildResponseDto(TransactionType.CREDIT_CHARGE);

        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createCreditCharge(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createCreditPaymentShouldSucceedWhenDebtExists() {
        TransactionRequestDTO request = buildRequest(TransactionType.CREDIT_PAYMENT);
        Transaction tx = buildTransaction(TransactionType.CREDIT_PAYMENT);
        TransactionResponseDto expected = buildResponseDto(TransactionType.CREDIT_PAYMENT);

        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(true));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createCreditPayment(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createInternalTransferShouldPersistTransaction() {
        TransactionRequestDTO request = buildRequest(TransactionType.TRANSFER_INTERNAL);
        Transaction tx = buildTransaction(TransactionType.TRANSFER_INTERNAL);
        TransactionResponseDto expected = buildResponseDto(TransactionType.TRANSFER_INTERNAL);

        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createInternalTransfer(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createExternalTransferShouldPersistTransactionAndPublishEvent() {
        // Arrange
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .customerId("CUST123")
                .accountId("ACC456")
                .destinationAccountId("DEST789")
                .operationTypeId("BANK001")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        Transaction tx = Transaction.builder()
                .id("TX999")
                .customerId("CUST123")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        TransactionResponseDto expected = TransactionResponseDto.builder()
                .id("TX999")
                .customerId("CUST123")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        // Act
        StepVerifier.create(service.createExternalTransfer(request))
                .expectNext(expected)
                .verifyComplete();

        // Assert
        ArgumentCaptor<ExternalTransferRequested> captor = ArgumentCaptor.forClass(ExternalTransferRequested.class);
        verify(externalTransferProducer).publish(captor.capture());

        ExternalTransferRequested event = captor.getValue();

        assertEquals("TX999", event.getTransferId().toString());
        assertEquals("ACC456", event.getOriginAccountId().toString());
        assertEquals("DEST789", event.getDestinationAccountNumber().toString());
        assertEquals("BANK001", event.getBankCode().toString());
        assertEquals(100.0, event.getAmount());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void createDebitCardChargeShouldPersistTransactionWithOrigin() {
        TransactionRequestDTO request = buildRequest(TransactionType.DEBIT_CARD_CHARGE);
        Transaction tx = buildTransaction(TransactionType.DEBIT_CARD_CHARGE);
        tx.setOrigin(TransactionOrigin.DEBIT_CARD);
        TransactionResponseDto expected = buildResponseDto(TransactionType.DEBIT_CARD_CHARGE);

        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createDebitCardCharge(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createDebitCardPaymentShouldSucceedWithValidAmount() {
        TransactionRequestDTO request = buildRequest(TransactionType.DEBIT_CARD_PAYMENT);
        Transaction tx = buildTransaction(TransactionType.DEBIT_CARD_PAYMENT);
        tx.setOrigin(TransactionOrigin.DEBIT_CARD);

        TransactionResponseDto expected = buildResponseDto(TransactionType.DEBIT_CARD_PAYMENT);
        expected.setOrigin(TransactionOrigin.DEBIT_CARD);

        when(validator.validarMontoPositivo(any())).thenReturn(Mono.empty());
        when(validator.validarMontoMaximo(any(), any())).thenReturn(Mono.empty());
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createDebitCardPayment(request))
                .expectNextMatches(dto ->
                        dto != null &&
                                dto.getType() == TransactionType.DEBIT_CARD_PAYMENT &&
                                dto.getOrigin() == TransactionOrigin.DEBIT_CARD &&
                                dto.getAmount().compareTo(new BigDecimal("100.00")) == 0 &&
                                dto.getAccountId().equals("acc789") &&
                                dto.getCustomerId().equals("cust123")
                )
                .verifyComplete();
    }

    @Test
    void createDebitWithdrawalOrderedShouldSucceedWithSufficientBalance() {
        TransactionRequestDTO request = buildRequest(TransactionType.DEBIT_WITHDRAWAL);
        request.setProductId("card123");

        Transaction previousTx = new Transaction();
        previousTx.setAccountId("acc001");
        previousTx.setAmount(new BigDecimal("200.00"));

        Transaction tx = buildTransaction(TransactionType.DEBIT_WITHDRAWAL);
        tx.setAccountId("acc001");
        tx.setAmount(request.getAmount().negate());

        TransactionResponseDto expected = buildResponseDto(TransactionType.DEBIT_WITHDRAWAL);
        expected.setAccountId("acc001");

        when(repository.findByProductIdAndStateOrderByCreatedAtDesc(eq("card123"), any()))
                .thenReturn(Flux.just(previousTx));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(expected);

        StepVerifier.create(service.createDebitWithdrawalOrdered(request))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void findAllShouldReturnMappedTransactions() {
        Transaction tx = buildTransaction(TransactionType.DEPOSIT);
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEPOSIT);

        when(repository.findAll()).thenReturn(Flux.just(tx));
        when(mapper.toDto(tx)).thenReturn(dto);

        StepVerifier.create(service.findAll())
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnMappedTransaction() {
        TransactionResponseDto dto = buildResponseDto(TransactionType.WITHDRAWAL);

        when(cachedService.getByIdCached("tx123")).thenReturn(Mono.just(dto));

        StepVerifier.create(service.findById("tx123"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void updateShouldSucceedWhenNoOverdueDebt() {
        TransactionRequestDTO request = buildRequest(TransactionType.TRANSFER_INTERNAL);
        Transaction existing = buildTransaction(TransactionType.DEPOSIT);
        Transaction updated = buildTransaction(TransactionType.TRANSFER_INTERNAL);
        TransactionResponseDto dto = buildResponseDto(TransactionType.TRANSFER_INTERNAL);

        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(repository.findById("tx123")).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(updated));
        when(mapper.toDto(updated)).thenReturn(dto);

        StepVerifier.create(service.update("tx123", request))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void countByAccountIdAndTypeInShouldReturnCount() {
        List<TransactionType> types = List.of(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL);

        when(repository.countByAccountIdAndTypeIn(eq("acc789"), anyList()))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(service.countByAccountIdAndTypeIn("acc789", types))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void hasOverdueCreditTransactionsShouldReturnTrue() {
        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.hasOverdueCreditTransactions("cust123"))
                .expectNext(true)
                .verifyComplete();
    }

    /*
        @Test
        void validateVipEligibilityShouldReturnTrueWhenNoDebt() {
            when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                    .thenReturn(Mono.just(false));

            StepVerifier.create(service.validateVipEligibility("cust123"))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        void validatePymeEligibilityShouldReturnFalseWhenHasDebt() {
            when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(service.validatePymeEligibility("cust123"))
                    .expectNext(false)
                    .verifyComplete();
        }
    */
    @Test
    void payThirdPartyCreditProductShouldSucceedWhenDebtExists() {
        TransactionRequestDTO request = buildRequest(TransactionType.CREDIT_PAYMENT);
        request.setDebtorDni("dni123");
        Transaction tx = buildTransaction(TransactionType.CREDIT_PAYMENT);
        tx.setOrigin(TransactionOrigin.CREDIT);
        TransactionResponseDto dto = buildResponseDto(TransactionType.CREDIT_PAYMENT);
        dto.setOrigin(TransactionOrigin.CREDIT);

        when(repository.existsByCustomerIdAndTypeAndDateBeforeAndState(any(), any(), any(), any()))
                .thenReturn(Mono.just(true));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(dto);

        StepVerifier.create(service.payThirdPartyCreditProduct(request))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void processDebitCardPaymentShouldPersistTransaction() {
        TransactionRequestDTO request = buildRequest(TransactionType.PAYMENT);
        Transaction tx = buildTransaction(TransactionType.PAYMENT);
        tx.setOrigin(TransactionOrigin.DEBIT_CARD);
        TransactionResponseDto dto = buildResponseDto(TransactionType.PAYMENT);
        dto.setOrigin(TransactionOrigin.DEBIT_CARD);

        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(dto);

        StepVerifier.create(service.processDebitCardPayment(request))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void processOrderedDebitWithdrawalShouldSucceedWithSufficientFunds() {
        TransactionRequestDTO request = buildRequest(TransactionType.DEBIT_WITHDRAWAL);
        request.setProductId("card123");
        request.setAmount(new BigDecimal("50.00"));

        Transaction previous = buildTransaction(TransactionType.DEPOSIT);
        previous.setAmount(new BigDecimal("100.00"));

        Transaction tx = buildTransaction(TransactionType.DEBIT_WITHDRAWAL);
        tx.setAmount(new BigDecimal("-50.00"));
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEBIT_WITHDRAWAL);

        when(repository.findByProductIdAndStateOrderByCreatedAtDesc(eq("card123"), any()))
                .thenReturn(Flux.just(previous));
        when(mapper.mapToEntity(any())).thenReturn(tx);
        when(repository.save(any())).thenReturn(Mono.just(tx));
        when(mapper.toDto(any())).thenReturn(dto);

        StepVerifier.create(service.processOrderedDebitWithdrawal(request))
                .expectNext(dto)
                .verifyComplete();
    }

    /*
        @Test
        void getLast10CardTransactionsShouldReturnMappedList() {
            Transaction tx = buildTransaction(TransactionType.DEBIT_CARD_PAYMENT);
            TransactionResponseDto dto = buildResponseDto(TransactionType.DEBIT_CARD_PAYMENT);

            when(repository.findTop10ByCustomerIdAndStateOrderByCreatedAtDesc(eq("cust123"), any()))
                    .thenReturn(Flux.just(tx));
            when(mapper.toDto(tx)).thenReturn(dto);

            StepVerifier.create(service.getLast10CardTransactions("cust123"))
                    .expectNext(dto)
                    .verifyComplete();
        }
    */
    @Test
    void getActiveTransactionByIdShouldReturnDtoWhenFound() {
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEPOSIT);

        when(cachedService.getActiveByIdCached("tx001")).thenReturn(Mono.just(dto));

        StepVerifier.create(service.getActiveTransactionById("tx001"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getActiveTransactionByIdShouldThrowWhenNotFound() {
        when(cachedService.getActiveByIdCached("tx001"))
                .thenReturn(Mono.error(new ResourceNotFoundException("No existe la transacción")));

        StepVerifier.create(service.getActiveTransactionById("tx001"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }


    @Test
    void getTransactionByIdShouldReturnDtoWhenFound() {
        TransactionResponseDto dto = buildResponseDto(TransactionType.WITHDRAWAL);

        when(cachedService.getByIdCached("tx002")).thenReturn(Mono.just(dto));

        StepVerifier.create(service.getTransactionById("tx002"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getTransactionByIdShouldThrowWhenNotFound() {
        when(cachedService.getByIdCached("tx002"))
                .thenReturn(Mono.error(new ResourceNotFoundException("No existe la transacción")));

        StepVerifier.create(service.getTransactionById("tx002"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void deleteTransactionByLogicalStateShouldSetInactiveAndReturnDto() {
        Transaction tx = buildTransaction(TransactionType.DEPOSIT);
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEPOSIT);
        tx.setState(TransactionState.INACTIVE);

        when(repository.findByIdAndState("tx003", TransactionState.ACTIVE)).thenReturn(Mono.just(tx));
        when(repository.save(tx)).thenReturn(Mono.just(tx));
        when(mapper.toDto(tx)).thenReturn(dto);

        StepVerifier.create(service.deleteTransactionByLogicalState("tx003"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void deleteTransactionByLogicalStateShouldThrowWhenNotFound() {
        when(repository.findByIdAndState("tx003", TransactionState.ACTIVE)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteTransactionByLogicalState("tx003"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getTransactionsByCustomerIdShouldReturnMappedFlux() {
        Transaction tx = buildTransaction(TransactionType.DEPOSIT);
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEPOSIT);

        when(repository.findByCustomerIdAndState("cust123", TransactionState.ACTIVE)).thenReturn(Flux.just(tx));
        when(mapper.toDto(tx)).thenReturn(dto);

        StepVerifier.create(service.getTransactionsByCustomerId("cust123"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getTransactionsByProductIdShouldReturnMappedFlux() {
        Transaction tx = buildTransaction(TransactionType.CREDIT_PAYMENT);
        TransactionResponseDto dto = buildResponseDto(TransactionType.CREDIT_PAYMENT);

        when(repository.findByProductIdAndState("prod456", TransactionState.ACTIVE)).thenReturn(Flux.just(tx));
        when(mapper.toDto(tx)).thenReturn(dto);

        StepVerifier.create(service.getTransactionsByProductId("prod456"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getTransactionsByDateRangeShouldReturnMappedFlux() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(5);
        OffsetDateTime end = OffsetDateTime.now();

        Transaction tx = buildTransaction(TransactionType.TRANSFER_INTERNAL);
        TransactionResponseDto dto = buildResponseDto(TransactionType.TRANSFER_INTERNAL);

        when(repository.findByCreatedAtBetweenAndState(start, end, TransactionState.ACTIVE)).thenReturn(Flux.just(tx));
        when(mapper.toDto(tx)).thenReturn(dto);

        StepVerifier.create(service.getTransactionsByDateRange(start, end))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void getLastTransactionShouldReturnDto() {
        TransactionResponseDto dto = buildResponseDto(TransactionType.DEBIT_CARD_PAYMENT);

        when(cachedService.getLastByCustomerIdCached("cust123")).thenReturn(Mono.just(dto));

        StepVerifier.create(service.getLastTransaction("cust123"))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void calculateTransactionFeeShouldReturnZeroWhenUnderLimit() {
        ReflectionTestUtils.setField(service, "transactionLimit", 10);
        when(repository.countByAccountIdAndTypeIn("acc789", List.of("DEPOSIT"))).thenReturn(Mono.just(3L));

        StepVerifier.create(service.calculateTransactionFee("acc789", TransactionType.DEPOSIT))
                .expectNext(0.0)
                .verifyComplete();
    }

    @Test
    void calculateTransactionFeeShouldReturnFixedFeeWhenOverLimit() {
        when(repository.countByAccountIdAndTypeIn("acc789", List.of("DEPOSIT"))).thenReturn(Mono.just(100L));

        StepVerifier.create(service.calculateTransactionFee("acc789", TransactionType.DEPOSIT))
                .expectNext(5.0)
                .verifyComplete();
    }
/*
    @Test
    void generateCustomerBalanceReportShouldReturnAggregatedDto() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(10);
        OffsetDateTime end = OffsetDateTime.now();

        Transaction tx1 = buildTransaction(TransactionType.DEPOSIT);
        tx1.setAmount(new BigDecimal("100.00"));
        Transaction tx2 = buildTransaction(TransactionType.DEPOSIT);
        tx2.setAmount(new BigDecimal("200.00"));

        when(repository.findByCustomerIdAndStateAndCreatedAtBetween("cust123", TransactionState.ACTIVE, start, end))
                .thenReturn(Flux.just(tx1, tx2));

        StepVerifier.create(service.generateCustomerBalanceReport("cust123", start, end))
                .expectNextMatches(dto ->
                        dto.getTotalAmount() == 300.0 &&
                                dto.getAverageBalance() == 150.0
                )
                .verifyComplete();
    }
*/

}
