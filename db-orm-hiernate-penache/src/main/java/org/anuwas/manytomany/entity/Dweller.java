package org.anuwas.manytomany.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Dweller {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String name;
    private String gender;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Bank> bankList;

}
