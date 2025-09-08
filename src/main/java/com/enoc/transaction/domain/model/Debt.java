package com.enoc.transaction.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;

/*
 Entidad del dominio que representa una deuda activa.
 Contiene lógica de negocio para validación y liquidación.
 */
@Getter
public class Debt {

    private final String id;
    private final String debtorDni;
    private BigDecimal amount;
    private final OffsetDateTime dueDate;

    public Debt(String id, String debtorDni, BigDecimal amount, OffsetDateTime dueDate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto inicial de la deuda debe ser positivo");
        }
        this.id = id;
        this.debtorDni = debtorDni;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    /*
     Reduce el monto de la deuda según el pago recibido.
     @param payment Monto a descontar.
     */
    public void applyPayment(BigDecimal payment) {
        if (payment == null || payment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El pago debe ser mayor a cero");
        }
        if (payment.compareTo(amount) > 0) {
            throw new IllegalArgumentException("El pago excede el monto de la deuda");
        }
        this.amount = this.amount.subtract(payment);
    }

    /*
     Verifica si la deuda ha sido saldada completamente.
     @return true si el monto es cero.
     */
    public boolean isSettled() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}