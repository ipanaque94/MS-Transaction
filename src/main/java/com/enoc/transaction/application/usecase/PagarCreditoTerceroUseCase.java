package com.enoc.transaction.application.usecase;

import com.enoc.transaction.application.internal.TransactionDomainService;
import com.enoc.transaction.domain.service.TransactionValidator;
import com.enoc.transaction.dto.request.TransactionRequestDTO;
import com.enoc.transaction.dto.response.TransactionResponseDto;
import com.enoc.transaction.mapper.TransactionMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso: Pago de producto de crédito de un tercero.
 * Orquesta validaciones, registro de transacción y actualización de deuda.
 */
@RequiredArgsConstructor
public class PagarCreditoTerceroUseCase {

    private final TransactionValidator validator;
    private final TransactionDomainService transactionDomainService;
    private final TransactionMapper mapper;

    public Mono<TransactionResponseDto> ejecutar(TransactionRequestDTO dto) {
        return validator.validarMontoPositivo(dto)
                .then(validator.validarMontoMaximo(dto, new BigDecimal("10000")))
                .then(validator.validarDeudaSemantica(dto.getAmount()))
                .then(transactionDomainService.validarDeudaExistente(dto.getDebtorDni(), dto.getAmount()))
                .flatMap(debt -> transactionDomainService.registrar(dto)
                        .flatMap(tx -> transactionDomainService.actualizarDeuda(debt, dto.getAmount())
                                .thenReturn(mapper.toDto(tx))
                        )
                );
    }
}
