package com.redhat.microservices.coffeeshop.storage.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name=Storage.NAME)
@Table(name=Storage.NAME)
@Schema(name=Storage.NAME, description="POJO that represents a storage record.")
@NamedQueries({
        @NamedQuery(name=Storage.QUERY_FIND_ALL, query="select s from Storage s"),
        @NamedQuery(name=Storage.QUERY_FIND_LAST, query="select s from Storage s order by s.creation desc")
})
public class Storage implements BaseModel<UUID> {

    public static final String NAME = "Storage";
    public static final String QUERY_FIND_ALL = "Storage.findAll";
    public static final String QUERY_FIND_LAST = "Storage.findLast";

    public Storage() { }

    public static enum Operation {
        IN, OUT;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "storageUUID")
    @GenericGenerator(
            name = "storageUUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Size(min = 1, max = 36, message = "Transaction ID must be between 1 and 36 characters")
    @Column(name = "transaction", nullable = true)
    private String transaction;

    @NotNull
    @Column(name="milk", nullable = false)
    @Digits(integer=5,fraction=2)
    private Double milk; // grams

    @NotNull
    @Column(name="totalMilk", nullable = false)
    @Digits(integer=7,fraction=2)
    private Double totalMilk; // grams

    @NotNull
    @Column(name="coffee", nullable = false)
    @Digits(integer=5,fraction=2)
    private Double coffee; // grams

    @NotNull
    @Column(name="totalCoffee", nullable = false)
    @Digits(integer=7,fraction=2)
    private Double totalCoffee; // grams

    @NotNull
    @Column(name="creation", nullable = false)
    @Parameter(hidden = true)
    private LocalDateTime creation;

    @NotNull
    @Column(name="op", nullable = false)
    private Storage.Operation op;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getMilk() {
        return milk;
    }

    public void setMilk(Double milk) {
        this.milk = milk;
    }

    public Double getTotalMilk() {
        return totalMilk;
    }

    public void setTotalMilk(Double totalMilk) {
        this.totalMilk = totalMilk;
    }

    public Double getCoffee() {
        return coffee;
    }

    public void setCoffee(Double coffee) {
        this.coffee = coffee;
    }

    public Double getTotalCoffee() {
        return totalCoffee;
    }

    public void setTotalCoffee(Double totalCoffee) {
        this.totalCoffee = totalCoffee;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Storage storage = (Storage) o;
        return id.equals(storage.id) &&
                Objects.equals(transaction, storage.transaction) &&
                milk.equals(storage.milk) &&
                totalMilk.equals(storage.totalMilk) &&
                coffee.equals(storage.coffee) &&
                totalCoffee.equals(storage.totalCoffee) &&
                creation.equals(storage.creation) &&
                op == storage.op;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transaction, milk, totalMilk, coffee, totalCoffee, creation, op);
    }
}
