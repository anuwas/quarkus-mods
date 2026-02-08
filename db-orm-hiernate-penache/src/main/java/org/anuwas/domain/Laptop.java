package org.anuwas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Laptop extends PanacheEntityBase {

    /*
    This type of @ID declaration or custom ID declearion like (laptop_id) will not work
    if we extend PanacheEntity because PanacheEntity already has an ID field declared as Long id with @Id and @GeneratedValue annotations.

    If we want to manage our own ID fild then instend of extending PanacheEntity we have to extend PanacheEntityBase.

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    */

    @Id
    @Column(name = "laptop_id", nullable = false, unique = true)
    private int laptopId;

    @Column(nullable = true)
    private String brand;

    @Column(nullable = true)
    private String model;

}
