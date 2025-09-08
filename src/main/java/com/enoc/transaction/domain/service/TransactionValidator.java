package com.enoc.transaction.domain.service;

import com.enoc.transaction.dto.request.TransactionRequestDTO;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para validar reglas de negocio de transacciones.
 * No debe tener dependencias externas ni anotaciones de frameworks.
 */
public class TransactionValidator {

    /**
     * Valida que el monto de la transacción sea positivo.
     */
    public Mono<Void> validarMontoPositivo(TransactionRequestDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("El monto debe ser mayor a cero"));
        }
        return Mono.empty();
    }

    /**
     * Valida que el monto no exceda el límite permitido.
     */
    public Mono<Void> validarMontoMaximo(TransactionRequestDTO dto, BigDecimal limiteMaximo) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(limiteMaximo) > 0) {
            return Mono.error(new IllegalArgumentException("El monto excede el límite permitido"));
        }
        return Mono.empty();
    }

    /**
     * Validación placeholder para deuda. Debe delegarse a un servicio de aplicación.
     */
    public Mono<Void> validarDeudaSemantica(BigDecimal montoSolicitado) {
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("El monto solicitado debe ser válido"));
        }
        return Mono.empty();
    }
}