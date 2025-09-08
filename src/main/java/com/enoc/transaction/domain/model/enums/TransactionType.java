package com.enoc.transaction.domain.model.enums;

public enum TransactionType {
    DEPOSIT,              // Ingreso de fondos
    WITHDRAWAL,           // Retiro de fondos
    PAYMENT,              // Pago a entidad externa
    CREDIT_CHARGE,        // Cargo a línea de crédito
    CREDIT_PAYMENT,       // Abono a crédito
    TRANSFER_INTERNAL,    // Entre cuentas propias
    TRANSFER_EXTERNAL,    // A terceros
    DEBIT_CARD_CHARGE,// Cargo con tarjeta de débito
    DEBIT_WITHDRAWAL,
    DEBIT_CARD_PAYMENT;   // Pago con tarjeta de débito

    public boolean isCommissionable() {
        return this == DEPOSIT || this == WITHDRAWAL;
    }

}
