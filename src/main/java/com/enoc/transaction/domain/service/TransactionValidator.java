package com.enoc.transaction.domain.service;

import com.enoc.transaction.dto.request.TransactionRequestDTO;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TransactionValidator {

    /*
      Valida que el monto de la transacción sea positivo.
     */
    public Mono<Void> validarMontoPositivo(TransactionRequestDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("El monto debe ser mayor a cero"));
        }
        return Mono.empty();
    }

    /*
      Valida que el monto no exceda el límite permitido.
     */
    public Mono<Void> validarMontoMaximo(TransactionRequestDTO dto, BigDecimal limiteMaximo) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(limiteMaximo) > 0) {
            return Mono.error(new IllegalArgumentException("El monto excede el límite permitido"));
        }
        return Mono.empty();
    }


}