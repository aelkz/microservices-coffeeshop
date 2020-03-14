package com.redhat.microservices.coffeeshop.product.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity(name=Product.NAME)
@Table(name=Product.NAME)
@Schema(name=Product.NAME, description="POJO that represents a product record.")
@NamedQueries({
        @NamedQuery(name= Product.QUERY_FIND_ALL,
                query="select p from Product p")
})
public class Product implements BaseModel<UUID> {

    public static final String NAME = "Product";
    public static final String QUERY_FIND_ALL = "Product.findAll";

    // ESPRESSO, ESPRESSO_DOPPIO, CAPPUCCINO, MACCHIATO, COLD_BREW, FRAPPE, AERO, FILTERED
    public Product() { }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "productUUID")
    @GenericGenerator(
            name = "productUUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Column(name="milk", nullable = true)
    @Digits(integer=3,fraction=2)
    private Double milk; // grams

    @NotNull
    @Column(name="coffee", nullable = false)
    @Digits(integer=3,fraction=2)
    private Double coffee; // grams

    @NotNull
    @javax.validation.constraints.Size(min = 3, max = 255, message = "Name must contain between 10 and 255 characters.")
    @Column(name="name", nullable = false)
    private String name;

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

    public Double getCoffee() {
        return coffee;
    }

    public void setCoffee(Double coffee) {
        this.coffee = coffee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id) &&
                milk.equals(product.milk) &&
                coffee.equals(product.coffee) &&
                name.equals(product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, milk, coffee, name);
    }
}
