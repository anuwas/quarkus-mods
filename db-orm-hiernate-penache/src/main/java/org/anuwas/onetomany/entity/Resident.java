package org.anuwas.onetomany.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String residentName;
    private String gender;

    /*
    if we declare mappedBy = "resident", then no extra column will be created inside
    resident table.
    by using CascadeType.ALL multple simcard and a Resident can be creatd at a time under
    single request
     */
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL,fetch=FetchType.EAGER)
    @JsonManagedReference
    List<SimCard> simCard;
}
