package com.redhat.microservices.coffeeshop.barista.model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.Objects;
import java.util.UUID;

@Entity(name=Barista.NAME)
@Table(name=Barista.NAME)
@Schema(name=Barista.NAME, description="POJO that represents a barista record.")
@NamedQueries({
        @NamedQuery(name = Barista.QUERY_FIND_ALL, query = "SELECT b FROM Barista b")
})
public class Barista implements BaseModel<UUID> {

    public static final String NAME = "Barista";
    public static final String QUERY_FIND_ALL = "Barista.findAll";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "baristaUUID")
    @GenericGenerator(
            name = "baristaUUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    /*
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "barista_generator")
    @SequenceGenerator(name="barista_generator", sequenceName = "barista_seq", allocationSize=50)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    */

    @NotNull
    @Size(min = 5, max = 255, message = "E-mail must be between 5 and 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    @Email(message = "Invalid email")
    private String email;

    @NotNull
    @Size(min = 5, max = 255, message = "Name must be between 5 and 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    public Barista() { }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        Barista barista = (Barista) o;
        return id.equals(barista.id) &&
                email.equals(barista.email) &&
                name.equals(barista.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name);
    }
}
