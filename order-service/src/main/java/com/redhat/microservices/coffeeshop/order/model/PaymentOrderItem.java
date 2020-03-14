package com.redhat.microservices.coffeeshop.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

@Entity(name=PaymentOrderItem.NAME)
@Table(name=PaymentOrderItem.NAME)
@Schema(name="PaymentOrderItem", description="POJO that represents a payment order item record.")
@NamedQueries({
        @NamedQuery(name= PaymentOrderItem.QUERY_FIND_ALL_BY_ORDER,
                query="select poi " +
                        "from PaymentOrderItem poi " +
                        "inner join poi.paymentOrder po " +
                        "where po.id = :paymentOrderId")
})
public class PaymentOrderItem implements BaseModel<UUID> {

    public static final String NAME = "PaymentOrderItem";
    public static final String QUERY_FIND_ALL_BY_ORDER = "PaymentOrderItem.findByPaymentOrderId";

    public static enum Size {
        S, M, L, NA;
    }

    public PaymentOrderItem() { }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "paymentOrderItemUUID")
    @GenericGenerator(
            name = "paymentOrderItemUUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @NotNull
    @javax.validation.constraints.Size(min = 1, max = 36, message = "Product ID must contain between 1 and 36 characters.")
    @Column(name="productId", nullable = false)
    private String productId;

    @NotNull
    @javax.validation.constraints.Size(min = 5, max = 255, message = "Description must contain between 5 and 255 characters.")
    @Column(name="description", nullable = false)
    private String description;

    @NotNull
    @Column(name="size", nullable = false)
    private PaymentOrderItem.Size size;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name="paymentOrder", referencedColumnName = "id")
    private PaymentOrder paymentOrder;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public PaymentOrder getPaymentOrder() {
        return paymentOrder;
    }

    public void setPaymentOrder(PaymentOrder paymentOrder) {
        this.paymentOrder = paymentOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentOrderItem that = (PaymentOrderItem) o;
        return id.equals(that.id) &&
                productId.equals(that.productId) &&
                description.equals(that.description) &&
                size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, description, size);
    }
}
