package org.anuwas.onetomany.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SimCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simCardNumber;
    private String provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    Resident resident;

}
