package com.redhat.microservices.coffeeshop.order.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity(name=PaymentOrder.NAME)
@Table(name=PaymentOrder.NAME)
@Schema(name="PaymentOrder", description="POJO that represents a payment order record.")
@NamedQueries({
        @NamedQuery(name = PaymentOrder.QUERY_FIND_ALL, query = "SELECT po FROM PaymentOrder po")
})
public class PaymentOrder implements BaseModel<UUID> {

    public static final String NAME = "PaymentOrder";
    public static final String QUERY_FIND_ALL = "PaymentOrder.findAll";

    public static enum PaymentMethod {
        CREDIT_CARD, MONEY;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "paymentOrderUUID")
    @GenericGenerator(
            name = "paymentOrderUUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    @Parameter(hidden = true)
    private UUID id;

    @Size(min = 1, max = 20, message = "Social Security Number or equivalent citizen ID must be between 1 and 20 characters")
    @Column(name = "ssn", nullable = true)
    private String ssn; // optional

    @NotNull
    @Column(name="creation", nullable = false)
    @Parameter(hidden = true)
    private LocalDateTime creation;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "paymentOrder")
    private Set<PaymentOrderItem> items;

    @NotNull
    @Column(name="paymentMethod", nullable = false)
    private PaymentOrder.PaymentMethod paymentMethod;

    public PaymentOrder() { }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Set<PaymentOrderItem> getItems() {
        return items;
    }

    public void setItems(Set<PaymentOrderItem> items) {
        this.items = items;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentOrder paymentOrder = (PaymentOrder) o;
        return id.equals(paymentOrder.id) &&
                Objects.equals(ssn, paymentOrder.ssn) &&
                creation.equals(paymentOrder.creation) &&
                items.equals(paymentOrder.items) &&
                paymentMethod == paymentOrder.paymentMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ssn, creation, items, paymentMethod);
    }
}
