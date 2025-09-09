package com.enoc.transaction.controller;

import com.enoc.transaction.application.service.TransactionService;
import com.enoc.transaction.domain.model.enums.StatusEnum;
import com.enoc.transaction.domain.model.enums.TransactionOrigin;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.infrastructure.rest.TransactionController;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@WebFluxTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService transactionService;


    // 1. Test: Create transaction (Debe crear una transacción)
    @Test
    void createShouldReturn201() {
        // Arrange: build request DTO
        TransactionRequestDTO requestDto = new TransactionRequestDTO();
        requestDto.setProductId("P001");
        requestDto.setAccountId("A001");
        requestDto.setOperationTypeId("OP01");
        requestDto.setType(TransactionType.TRANSFER_INTERNAL);
        requestDto.setOrigin(TransactionOrigin.DEBIT_CARD);
        requestDto.setStatus(StatusEnum.PENDING);
        requestDto.setAmount(BigDecimal.valueOf(100));
        requestDto.setDate(OffsetDateTime.now());
        requestDto.setEventDate(OffsetDateTime.now());
        requestDto.setCustomerId("C001");
        requestDto.setDestinationAccountId("A002");
        requestDto.setCommissionApplied(BigDecimal.ZERO);
        requestDto.setDescription("Test transaction");

        // Simulate service response
        TransactionResponseDto expectedDto = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(100))
                .build();

        when(transactionService.create(any(TransactionRequestDTO.class)))
                .thenReturn(Mono.just(expectedDto));

        // Act & Assert
        webTestClient.post()
                .uri("/api/transactions")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated() // Verifica que la respuesta sea 201 (Creado)
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(expectedDto);
    }

    // 2. Test: Get transaction by ID (Obtener transacción por ID)
    @Test
    void getByIdShouldReturnTransaction() {
        // Arrange: Prepare response DTO
        TransactionResponseDto response = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(100))
                .build();

        when(transactionService.findById("tx123")).thenReturn(Mono.just(response));

        // Act & Assert: Make request and validate response
        webTestClient.get()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isOk()  // Verifica que el status sea OK (200)
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(response);  // Verifica que el cuerpo de la respuesta sea igual al esperado
    }

    // 3. Test: Get all transactions (Obtener todas las transacciones)
    @Test
    void getAllShouldReturnFlux() {
        // Arrange: Create test transactions
        TransactionResponseDto tx1 = TransactionResponseDto.builder()
                .id("1")
                .amount(BigDecimal.valueOf(100))
                .build();

        TransactionResponseDto tx2 = TransactionResponseDto.builder()
                .id("2")
                .amount(BigDecimal.valueOf(200))
                .build();

        when(transactionService.findAll()).thenReturn(Flux.just(tx1, tx2));

        // Act & Assert: Validate the response body
        webTestClient.get()
                .uri("/api/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponseDto.class)
                .hasSize(2)
                .contains(tx1, tx2);
    }

    // 4. Test: Update transaction (Actualizar transacción)
    @Test
    void updateShouldReturnUpdatedTransaction() {
        // Arrange: Prepare request and expected response
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .amount(BigDecimal.valueOf(150))
                .build();

        TransactionResponseDto response = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(150))
                .build();

        when(transactionService.update(eq("tx123"), any())).thenReturn(Mono.just(response));

        // Act & Assert: Make update request and validate response
        webTestClient.put()
                .uri("/api/transactions/tx123")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()  // Verifica que el status sea OK (200)
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(response);  // Verifica que la transacción actualizada sea correcta
    }

    // 5. Test: Delete transaction (Eliminar transacción)
    @Test
    void deleteShouldReturnNoContent() {
        when(transactionService.deleteTransactionByLogicalState("tx123"))
                .thenReturn(Mono.just(new TransactionResponseDto()));

        // Act & Assert: Verify that the delete request returns no content (204)
        webTestClient.delete()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isNoContent();  // Verifica que el status sea No Content (204)
    }

    // 6. Test: Count transactions by account and type (Contar transacciones por cuenta y tipo)
    @Test
    void shouldReturnCountForTransferTypes() {
        // Arrange: Prepare test data
        String accountId = "ACC123";
        List<TransactionType> types = List.of(
                TransactionType.TRANSFER_INTERNAL,
                TransactionType.TRANSFER_EXTERNAL
        );
        Long expectedCount = 7L;

        Mockito.when(transactionService.countByAccountIdAndTypeIn(accountId, types))
                .thenReturn(Mono.just(expectedCount));

        String[] typeNames = types.stream()
                .map(Enum::name)
                .toArray(String[]::new);

        // Act & Assert: Make request and verify the count
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transactions/count")
                        .queryParam("accountId", accountId)
                        .queryParam("types", (Object[]) typeNames)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(expectedCount);

        // Verify: Ensure service method was called
        Mockito.verify(transactionService).countByAccountIdAndTypeIn(accountId, types);
    }

    // 7. Test: Get transactions by product ID (Obtener transacciones por productId)
    @Test
    void getTransactionsByProductIdShouldReturnTransactions() {
        // Arrange: Configurar la respuesta esperada para las transacciones
        TransactionResponseDto tx1 = TransactionResponseDto.builder()
                .id("1")
                .amount(BigDecimal.valueOf(100))
                .productId("P001")
                .build();

        TransactionResponseDto tx2 = TransactionResponseDto.builder()
                .id("2")
                .amount(BigDecimal.valueOf(200))
                .productId("P001")
                .build();

        // Simular la respuesta del servicio
        when(transactionService.getTransactionsByProductId("P001")).thenReturn(Flux.just(tx1, tx2));

        // Act & Assert: Verificar la respuesta de la API
        webTestClient.get()
                .uri("/api/transactions/product/P001")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponseDto.class)
                .hasSize(2)
                .contains(tx1, tx2);
    }


    // 8. Test: Get transactions by date range (Obtener transacciones por rango de fechas)
    @Test
    void getTransactionsByDateRangeShouldReturnTransactions() {
        // Arrange: Prepare test dates using OffsetDateTime
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(10); // 10 días atrás
        OffsetDateTime endDate = OffsetDateTime.now();  // Fecha actual

        // Crear transacciones de prueba
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

        // Simula la respuesta del servicio
        when(transactionService.getTransactionsByDateRange(startDate, endDate))
                .thenReturn(Flux.just(tx1, tx2));

        // Act & Assert: Realizar la solicitud y verificar la respuesta
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transactions/date-range")
                        .queryParam("start", startDate.toString())  // Asegúrate de usar el formato correcto ISO
                        .queryParam("end", endDate.toString())  // Asegúrate de usar el formato correcto ISO
                        .build())
                .exchange()
                .expectStatus().isOk()  // Verifica que el status sea OK (200)
                .expectBodyList(TransactionResponseDto.class)
                .hasSize(2)  // Verifica que la lista contiene 2 elementos
                .contains(tx1, tx2);  // Verifica que los elementos contienen las transacciones esperadas
    }

    // 9. Test: Verificar si el cliente tiene deudas vencidas
    @Test
    void hasOverdueCreditTransactionsShouldReturnBoolean() {
        // Arrange: Configurar la respuesta esperada
        String customerId = "C001";
        Boolean expected = true;

        when(transactionService.hasOverdueCreditTransactions(customerId)).thenReturn(Mono.just(expected));

        // Act & Assert: Verificar la respuesta de la API
        webTestClient.get()
                .uri("/api/transactions/has-overdue-debts/{customerId}", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(expected);
    }

    // 10. Test: Pagar un producto de crédito de terceros
    @Test
    void payThirdPartyCreditProductShouldReturnTransaction() {
        // Arrange: Configurar el DTO de la solicitud y la respuesta esperada
        TransactionRequestDTO requestDto = new TransactionRequestDTO();
        requestDto.setAmount(BigDecimal.valueOf(100));
        requestDto.setCustomerId("C001");
        requestDto.setProductId("P001");

        TransactionResponseDto expectedResponse = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(100))
                .build();

        // Simular la respuesta del servicio
        when(transactionService.payThirdPartyCreditProduct(requestDto)).thenReturn(Mono.just(expectedResponse));

        // Act & Assert: Verificar la respuesta de la API
        webTestClient.post()
                .uri("/api/transactions/payment/third-party")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(expectedResponse);
    }

    // 11. Test: Procesar pago con tarjeta de débito
    @Test
    void processDebitCardPaymentShouldReturnTransaction() {
        // Arrange: Configurar el DTO de la solicitud y la respuesta esperada
        TransactionRequestDTO requestDto = new TransactionRequestDTO();
        requestDto.setAmount(BigDecimal.valueOf(150));

        TransactionResponseDto expectedResponse = TransactionResponseDto.builder()
                .id("tx124")
                .amount(BigDecimal.valueOf(150))
                .build();

        // Simular la respuesta del servicio
        when(transactionService.processDebitCardPayment(requestDto)).thenReturn(Mono.just(expectedResponse));

        // Act & Assert: Verificar la respuesta de la API
        webTestClient.post()
                .uri("/api/transactions/payment/debit-card")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(expectedResponse);
    }

    // 12. Test: Procesar retiro con tarjeta de débito
    @Test
    void processOrderedDebitWithdrawalShouldReturnTransaction() {
        // Arrange: Configurar el DTO de la solicitud y la respuesta esperada
        TransactionRequestDTO requestDto = new TransactionRequestDTO();
        requestDto.setAmount(BigDecimal.valueOf(200));

        TransactionResponseDto expectedResponse = TransactionResponseDto.builder()
                .id("tx125")
                .amount(BigDecimal.valueOf(200))
                .build();

        // Simular la respuesta del servicio
        when(transactionService.processOrderedDebitWithdrawal(requestDto)).thenReturn(Mono.just(expectedResponse));

        // Act & Assert: Verificar la respuesta de la API
        webTestClient.post()
                .uri("/api/transactions/withdrawal/debit-card")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(expectedResponse);
    }

}



