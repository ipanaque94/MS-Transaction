package com.enoc.transaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * TransactionRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-08-14T23:59:59.443330300-05:00[America/Lima]")
public class TransactionRequest {

  private String productId;

  /**
   * Tipo de transacción
   */
  public enum TypeEnum {
    DEPOSIT("DEPOSIT"),
    
    WITHDRAWAL("WITHDRAWAL"),
    
    PAYMENT("PAYMENT"),
    
    CREDIT_CHARGE("CREDIT_CHARGE");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private TypeEnum type;

  private BigDecimal amount;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime date;

  private JsonNullable<String> description = JsonNullable.undefined();

  /**
   * Default constructor
   * @deprecated Use {@link TransactionRequest#TransactionRequest(String, TypeEnum, BigDecimal, OffsetDateTime)}
   */
  @Deprecated
  public TransactionRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransactionRequest(String productId, TypeEnum type, BigDecimal amount, OffsetDateTime date) {
    this.productId = productId;
    this.type = type;
    this.amount = amount;
    this.date = date;
  }

  public TransactionRequest productId(String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * ID del producto relacionado
   * @return productId
  */
  @NotNull 
  @Schema(name = "productId", description = "ID del producto relacionado", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("productId")
  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public TransactionRequest type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Tipo de transacción
   * @return type
  */
  @NotNull 
  @Schema(name = "type", description = "Tipo de transacción", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public TransactionRequest amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Monto de la transacción
   * minimum: 0.01
   * @return amount
  */
  @NotNull @Valid @DecimalMin("0.01") 
  @Schema(name = "amount", description = "Monto de la transacción", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public TransactionRequest date(OffsetDateTime date) {
    this.date = date;
    return this;
  }

  /**
   * Fecha y hora de la transacción
   * @return date
  */
  @NotNull @Valid 
  @Schema(name = "date", description = "Fecha y hora de la transacción", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("date")
  public OffsetDateTime getDate() {
    return date;
  }

  public void setDate(OffsetDateTime date) {
    this.date = date;
  }

  public TransactionRequest description(String description) {
    this.description = JsonNullable.of(description);
    return this;
  }

  /**
   * Descripción opcional
   * @return description
  */
  
  @Schema(name = "description", description = "Descripción opcional", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public JsonNullable<String> getDescription() {
    return description;
  }

  public void setDescription(JsonNullable<String> description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionRequest transactionRequest = (TransactionRequest) o;
    return Objects.equals(this.productId, transactionRequest.productId) &&
        Objects.equals(this.type, transactionRequest.type) &&
        Objects.equals(this.amount, transactionRequest.amount) &&
        Objects.equals(this.date, transactionRequest.date) &&
        equalsNullable(this.description, transactionRequest.description);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, type, amount, date, hashCodeNullable(description));
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionRequest {\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

