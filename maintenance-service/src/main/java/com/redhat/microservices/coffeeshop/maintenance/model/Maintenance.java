package com.redhat.microservices.coffeeshop.maintenance.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name=Maintenance.NAME)
@Table(name=Maintenance.NAME)
@Schema(name=Maintenance.NAME, description="POJO that represents a maintenance record.")
@NamedQueries({
        @NamedQuery(name = Maintenance.QUERY_FIND_ALL, query = "SELECT m FROM Maintenance m")
})
public class Maintenance implements BaseModel<UUID> {

    public static final String NAME = "Maintenance";
    public static final String QUERY_FIND_ALL = "Maintenance.findAll";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "maintenanceUUID")
    @GenericGenerator(
            name = "maintenanceUUID",
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
    @Size(min = 1, max = 36, message = "Barista ID must be between 1 and 36 characters")
    @Column(name = "barista", nullable = false)
    private String barista;

    @Column(name="creation", nullable = false)
    private LocalDateTime creation;

    public Maintenance() { }

    public Maintenance(String barista) {
        this.barista = barista;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBarista() {
        return barista;
    }

    public void setBarista(String barista) {
        this.barista = barista;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Maintenance that = (Maintenance) o;
        return id.equals(that.id) &&
                barista.equals(that.barista) &&
                creation.equals(that.creation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barista, creation);
    }
}
