package org.anuwas.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Desktop {
    @Id
    @Column(name = "desktop_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int desktopId;

    @Column(nullable = true)
    private String brand;

    @Column(nullable = true)
    private String model;
}
