package com.enoc.transaction.infrastructure.rest;

import com.enoc.transaction.application.service.TransactionService;
import com.enoc.transaction.domain.model.enums.TransactionType;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Mono<ResponseEntity<TransactionResponseDto>> create(@RequestBody Mono<TransactionRequestDTO> request) {
        return request
                .flatMap(transactionService::create)
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponseDto>> getById(@PathVariable String id) {
        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Flux<TransactionResponseDto> getAll() {
        return transactionService.findAll();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponseDto>> update(
            @PathVariable String id,
            @RequestBody Mono<TransactionRequestDTO> request) {
        return request
                .flatMap(dto -> transactionService.update(id, dto))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return transactionService.deleteTransactionByLogicalState(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    /*
      Count the number of transactions for a specific account and type.
      Contar la cantidad de transacciones para una cuenta específica y tipo.
     */
    @GetMapping("/count")
    public Mono<Long> countByAccountIdAndType(@RequestParam String accountId, @RequestParam List<TransactionType> types) {
        return transactionService.countByAccountIdAndTypeIn(accountId, types);
    }

    /*
      Get transactions for a specific product.
      Obtener transacciones para un producto específico.
     */
    @GetMapping("/product/{productId}")
    public Flux<TransactionResponseDto> getTransactionsByProductId(@PathVariable String productId) {
        return transactionService.getTransactionsByProductId(productId);
    }

    /*
      Get transactions within a specified date range.
      Obtener transacciones dentro de un rango de fechas especificado.
     */
    @GetMapping("/date-range")
    public Flux<TransactionResponseDto> getTransactionsByDateRange(
            @RequestParam String start,  // Recibir como String
            @RequestParam String end) {  // Recibir como String

        OffsetDateTime startDateTime = OffsetDateTime.parse(start);  // Parsear la fecha a OffsetDateTime
        OffsetDateTime endDateTime = OffsetDateTime.parse(end);

        return transactionService.getTransactionsByDateRange(startDateTime, endDateTime);
    }


    /*
      Check if a customer has overdue credit transactions.
      Verificar si un cliente tiene transacciones de crédito vencidas.
     */
    @GetMapping("/has-overdue-debts/{customerId}")
    public Mono<ResponseEntity<Boolean>> hasOverdueCreditTransactions(@PathVariable String customerId) {
        return transactionService.hasOverdueCreditTransactions(customerId)
                .map(ResponseEntity::ok) // Return the result as boolean
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Return 404 if not found
    }

    /*
      Pay a third-party credit product.
      Pagar un producto de crédito de terceros.
     */
    @PostMapping("/payment/third-party")
    public Mono<ResponseEntity<TransactionResponseDto>> payThirdPartyCreditProduct(
            @RequestBody TransactionRequestDTO request) {
        return transactionService.payThirdPartyCreditProduct(request)
                .map(response -> ResponseEntity.status(201).body(response)) // Return status 201 if payment is successful
                .onErrorReturn(ResponseEntity.badRequest().build()); // Return 400 if there's an error
    }

    /*
      Process payment with debit card.
      Procesar el pago con tarjeta de débito.
     */
    @PostMapping("/payment/debit-card")
    public Mono<ResponseEntity<TransactionResponseDto>> processDebitCardPayment(
            @RequestBody TransactionRequestDTO request) {
        return transactionService.processDebitCardPayment(request)
                .map(response -> ResponseEntity.status(201).body(response)) // Return status 201 if payment is successful
                .onErrorReturn(ResponseEntity.badRequest().build()); // Return 400 if there's an error
    }

    /*
      Process withdrawal with debit card according to the order of associated accounts.
      Procesar un retiro con tarjeta de débito según el orden de las cuentas asociadas.
     */
    // Método para procesar un retiro con tarjeta de débito según la cuenta seleccionada
    @PostMapping("/withdrawal/debit-card")
    public Mono<ResponseEntity<TransactionResponseDto>> processOrderedDebitWithdrawal(
            @RequestBody TransactionRequestDTO request) {
        return transactionService.processOrderedDebitWithdrawal(request)
                .map(response -> ResponseEntity.status(201).body(response))  // Respuesta 201 (Creado) si el retiro es exitoso
                .onErrorReturn(ResponseEntity.badRequest().build());  // Si ocurre un error, devuelve 400 (Bad Request)
    }


    /*
      Get the last 10 debit card transactions.
      Obtener los últimos 10 movimientos de la tarjeta de débito.
     */
    @GetMapping("/debit-card/{cardId}/last-10-transactions")
    public Flux<TransactionResponseDto> getLast10CardTransactions(@PathVariable String cardId) {
        return transactionService.getLast10CardTransactions(cardId);
    }

    /*
      Get report
      Obtener reporte.
     */

    @GetMapping("/balance-report")
    public Mono<ResponseEntity<TransactionResponseDto>> generateBalanceReport(
            @RequestParam String customerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        OffsetDateTime startDateTime = OffsetDateTime.parse(startDate);  // Suponiendo que recibes las fechas en formato ISO
        OffsetDateTime endDateTime = OffsetDateTime.parse(endDate);

        // Llamar al servicio para generar el reporte
        return transactionService.generateCustomerBalanceReport(customerId, startDateTime, endDateTime)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}