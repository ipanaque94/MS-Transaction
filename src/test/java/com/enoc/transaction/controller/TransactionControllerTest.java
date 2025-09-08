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


    @Test
    void createShouldReturn201() {
        // Arrange: construir el DTO de entrada
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

        // Simular la respuesta del servicio
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
                .expectStatus().isCreated()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void getByIdShouldReturnTransaction() {
        TransactionResponseDto response = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(100))
                .build();

        when(transactionService.findById("tx123")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(response);
    }

    @Test
    void getAllShouldReturnFlux() {
        TransactionResponseDto tx1 = TransactionResponseDto.builder()
                .id("1")
                .amount(BigDecimal.valueOf(100))
                .build();

        TransactionResponseDto tx2 = TransactionResponseDto.builder()
                .id("2")
                .amount(BigDecimal.valueOf(200))
                .build();

        when(transactionService.findAll()).thenReturn(Flux.just(tx1, tx2));

        webTestClient.get()
                .uri("/api/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponseDto.class)
                .hasSize(2)
                .contains(tx1, tx2);
    }

    @Test
    void updateShouldReturnUpdatedTransaction() {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .amount(BigDecimal.valueOf(150))
                .build();

        TransactionResponseDto response = TransactionResponseDto.builder()
                .id("tx123")
                .amount(BigDecimal.valueOf(150))
                .build();

        when(transactionService.update(eq("tx123"), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/transactions/tx123")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponseDto.class)
                .isEqualTo(response);
    }

    @Test
    void deleteShouldReturnNoContent() {
        when(transactionService.deleteTransactionByLogicalState("tx123"))
                .thenReturn(Mono.just(new TransactionResponseDto()));

        webTestClient.delete()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnCountForTransferTypes() {
        // Arrange
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


        // Act & Assert
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

        // Verify
        Mockito.verify(transactionService).countByAccountIdAndTypeIn(accountId, types);
    }


}



