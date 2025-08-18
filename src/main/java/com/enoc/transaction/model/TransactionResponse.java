package com.enoc.transaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * TransactionResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-08-14T23:59:59.443330300-05:00[America/Lima]")
public class TransactionResponse {

  private String id;

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

  public TransactionResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * ID único de la transacción
   * @return id
  */
  
  @Schema(name = "id", description = "ID único de la transacción", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TransactionResponse productId(String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * ID del producto relacionado
   * @return productId
  */
  
  @Schema(name = "productId", description = "ID del producto relacionado", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productId")
  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public TransactionResponse type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Tipo de transacción
   * @return type
  */
  
  @Schema(name = "type", description = "Tipo de transacción", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public TransactionResponse amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Monto de la transacción
   * @return amount
  */
  @Valid 
  @Schema(name = "amount", description = "Monto de la transacción", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public TransactionResponse date(OffsetDateTime date) {
    this.date = date;
    return this;
  }

  /**
   * Fecha y hora de la transacción
   * @return date
  */
  @Valid 
  @Schema(name = "date", description = "Fecha y hora de la transacción", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("date")
  public OffsetDateTime getDate() {
    return date;
  }

  public void setDate(OffsetDateTime date) {
    this.date = date;
  }

  public TransactionResponse description(String description) {
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
    TransactionResponse transactionResponse = (TransactionResponse) o;
    return Objects.equals(this.id, transactionResponse.id) &&
        Objects.equals(this.productId, transactionResponse.productId) &&
        Objects.equals(this.type, transactionResponse.type) &&
        Objects.equals(this.amount, transactionResponse.amount) &&
        Objects.equals(this.date, transactionResponse.date) &&
        equalsNullable(this.description, transactionResponse.description);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, productId, type, amount, date, hashCodeNullable(description));
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
    sb.append("class TransactionResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

