package com.enoc.transaction.service;

import com.enoc.transaction.domain.model.Transaction;
import com.enoc.transaction.domain.model.enums.TransactionState;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.domain.repository.TransactionRepository;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.mapper.TransactionMapper;
import com.enoc.transaction.infrastructure.service.TransactionServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequestDTO requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new TransactionRequestDTO();
        requestDto.setAmount(BigDecimal.valueOf(200));
        requestDto.setCardId("CARD123");
        requestDto.setCustomerId("CUSTOMER123");
    }

    // Test for the 'create' method
    @Test
    void createShouldReturnTransactionResponse() {
        // Arrange: Configura la transacción simulada
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));

        // Configura el comportamiento esperado de los mocks
        when(repository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));
        when(mapper.toDto(any(Transaction.class))).thenReturn(TransactionResponseDto.builder().amount(BigDecimal.valueOf(100)).build());

        // Act: Llamar al método del servicio
        Mono<TransactionResponseDto> response = transactionService.create(requestDto);

        // Assert: Verificar la respuesta
        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getAmount().equals(BigDecimal.valueOf(100)))  // Verifica el monto esperado
                .verifyComplete();  // Verifica que la secuencia se haya completado correctamente
    }

    // Test for the 'processOrderedDebitWithdrawal' method
    @Test
    void processOrderedDebitWithdrawalShouldReturnTransaction() {
        // Arrange: Configuración del DTO de la solicitud y la respuesta esperada
        TransactionRequestDTO requestDto = new TransactionRequestDTO();
        requestDto.setAmount(BigDecimal.valueOf(200));  // Monto solicitado
        requestDto.setCardId("CARD123");
        requestDto.setCustomerId("CUSTOMER123");

        // Configurar la transacción simulada
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(200));  // Transacción con suficiente saldo

        // Mock de repositorio: findByCardIdAndStateOrderByCreatedAtDesc (ahora devuelve Flux<Transaction>)
        when(repository.findByCardIdAndStateOrderByCreatedAtDesc(any(), eq(TransactionState.ACTIVE)))
                .thenReturn(Flux.just(transaction));  // Devuelve un Flux con la transacción simulada

        // Mock de la transacción guardada
        when(repository.save(any(Transaction.class)))
                .thenReturn(Mono.just(transaction));  // Simula el guardado de la transacción

        // Mock del mapeo a DTO
        when(mapper.toDto(any(Transaction.class)))
                .thenReturn(TransactionResponseDto.builder().amount(BigDecimal.valueOf(200)).build());  // Devuelve el DTO con el monto

        // Act: Llamar al servicio
        Mono<TransactionResponseDto> response = transactionService.processOrderedDebitWithdrawal(requestDto);

        // Assert: Verificar la respuesta
        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto
                        .getAmount().equals(BigDecimal.valueOf(200)))  // Verificar el monto
                .verifyComplete();  // Verificar que la secuencia se complete correctamente
    }


    // Test for the 'payThirdPartyCreditProduct' method
    @Test
    void payThirdPartyCreditProductShouldReturnTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));

        when(repository.save(any(Transaction.class))).thenReturn(Mono.just(transaction));
        when(mapper.toDto(any(Transaction.class))).thenReturn(TransactionResponseDto.builder().amount(BigDecimal.valueOf(100)).build());

        Mono<TransactionResponseDto> response = transactionService.payThirdPartyCreditProduct(requestDto);

        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getAmount().equals(BigDecimal.valueOf(100)))
                .verifyComplete();
    }

    // Test for the 'getLastTransaction' method
    @Test
    void getLastTransactionShouldReturnTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        when(repository.findTopByCustomerIdAndStateOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Mono.just(transaction));
        when(mapper.toDto(any(Transaction.class))).thenReturn(TransactionResponseDto.builder().amount(BigDecimal.valueOf(100)).build());

        Mono<TransactionResponseDto> response = transactionService.getLastTransaction("CUSTOMER123");

        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getAmount().equals(BigDecimal.valueOf(100)))
                .verifyComplete();
    }

    // Test for the 'getTransactionsByDateRange' method
    @Test
    void getTransactionsByDateRangeShouldReturnTransactions() {
        // Arrange: Preparar fechas de prueba usando OffsetDateTime
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(10); // 10 días atrás
        OffsetDateTime endDate = OffsetDateTime.now();  // Fecha actual

        // Crear respuestas de transacción de prueba
        TransactionResponseDto tx1 = TransactionResponseDto.builder()
                .id("1")
                .amount(BigDecimal.valueOf(100))
                .date(OffsetDateTime.now().minusDays(5))  // Fecha dentro del rango
                .build();

        TransactionResponseDto tx2 = TransactionResponseDto.builder()
                .id("2")
                .amount(BigDecimal.valueOf(200))
                .date(OffsetDateTime.now().minusDays(3))  // Fecha dentro del rango
                .build();

        // Mock de la respuesta del servicio
        when(transactionService.getTransactionsByDateRange(startDate, endDate))
                .thenReturn(Flux.just(tx1, tx2));

        // Act: Llamar al método de servicio
        Flux<TransactionResponseDto> response = transactionService.getTransactionsByDateRange(startDate, endDate);

        // Assert: Verificar la respuesta
        StepVerifier.create(response)
                .expectNext(tx1)
                .expectNext(tx2)
                .verifyComplete();
    }


    // Test for the 'countByAccountIdAndTypeIn' method
    @Test
    void countByAccountIdAndTypeInShouldReturnTransactionCount() {
        String accountId = "ACCOUNT123";
        List<TransactionType> types = List.of(TransactionType.TRANSFER_INTERNAL, TransactionType.TRANSFER_EXTERNAL);
        Long expectedCount = 10L;

        when(repository.countByAccountIdAndTypeIn(accountId, List.of("TRANSFER_INTERNAL", "TRANSFER_EXTERNAL")))
                .thenReturn(Mono.just(expectedCount));

        Mono<Long> response = transactionService.countByAccountIdAndTypeIn(accountId, types);

        StepVerifier.create(response)
                .expectNext(expectedCount)
                .verifyComplete();
    }

    @Test
    void getTransactionsByCustomerIdShouldReturnTransactions() {
        // Arrange: Configura los datos de prueba
        String customerId = "CUSTOMER123";
        Transaction tx1 = new Transaction();
        tx1.setId("1");
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setState(TransactionState.ACTIVE);

        Transaction tx2 = new Transaction();
        tx2.setId("2");
        tx2.setAmount(BigDecimal.valueOf(200));
        tx2.setState(TransactionState.ACTIVE);

        // Simula la respuesta del repositorio
        when(repository.findByCustomerIdAndState(customerId, TransactionState.ACTIVE))
                .thenReturn(Flux.just(tx1, tx2));  // Devuelve un Flux<Transaction> del repositorio.

        // Simula el mapeo de las transacciones a DTO
        when(mapper.toDto(tx1)).thenReturn(TransactionResponseDto.builder().id("1").amount(BigDecimal.valueOf(100)).build());
        when(mapper.toDto(tx2)).thenReturn(TransactionResponseDto.builder().id("2").amount(BigDecimal.valueOf(200)).build());

        // Act: Llamar al método del servicio
        Flux<TransactionResponseDto> response = transactionService.getTransactionsByCustomerId(customerId);

        // Assert: Verificar la respuesta
        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getId().equals("1")
                        && transactionResponseDto.getAmount().equals(BigDecimal.valueOf(100)))
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getId().equals("2")
                        && transactionResponseDto.getAmount().equals(BigDecimal.valueOf(200)))
                .verifyComplete();  // Verifica que la secuencia se haya completado.
    }

    @Test
    void getTransactionsByProductIdShouldReturnTransactions() {
        String productId = "PRODUCT123";

        // Crear instancias de Transaction
        Transaction tx1 = new Transaction();
        tx1.setId("1");
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setProductId(productId);
        tx1.setState(TransactionState.ACTIVE);

        Transaction tx2 = new Transaction();
        tx2.setId("2");
        tx2.setAmount(BigDecimal.valueOf(200));
        tx2.setProductId(productId);
        tx2.setState(TransactionState.ACTIVE);

        // Simular la respuesta del repositorio (devuelve Flux<Transaction>)
        when(repository.findByProductIdAndState(productId, TransactionState.ACTIVE))
                .thenReturn(Flux.just(tx1, tx2));

        // Simular el mapeo a DTO
        when(mapper.toDto(tx1)).thenReturn(TransactionResponseDto.builder().id("1").amount(BigDecimal.valueOf(100)).build());
        when(mapper.toDto(tx2)).thenReturn(TransactionResponseDto.builder().id("2").amount(BigDecimal.valueOf(200)).build());

        // Act: Llamar al método del servicio
        Flux<TransactionResponseDto> response = transactionService.getTransactionsByProductId(productId);

        // Assert: Verificar la respuesta
        StepVerifier.create(response)
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getId().equals("1")
                        && transactionResponseDto.getAmount().equals(BigDecimal.valueOf(100)))
                .expectNextMatches(transactionResponseDto -> transactionResponseDto.getId().equals("2")
                        && transactionResponseDto.getAmount().equals(BigDecimal.valueOf(200)))
                .verifyComplete();
    }


    // Test for the 'generateCustomerBalanceReport' method
    @Test
    void generateCustomerBalanceReportShouldReturnReport() {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(10); // 10 days ago
        OffsetDateTime endDate = OffsetDateTime.now();  // Current date
        String customerId = "CUSTOMER123";

        TransactionResponseDto report = new TransactionResponseDto();
        report.setTotalAmount(1000.0);
        report.setAverageBalance(100.0);

        when(repository.findByCustomerIdAndStateAndCreatedAtBetween(customerId, TransactionState.ACTIVE, startDate, endDate))
                .thenReturn(Flux.just(new Transaction())); // Simulate transactions in the date range

        Mono<TransactionResponseDto> response = transactionService.generateCustomerBalanceReport(customerId, startDate, endDate);

        StepVerifier.create(response)
                .expectNext(report)
                .verifyComplete();
    }
}