package org.anuwas.onetoone.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Aadhar {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String aadharNumber;
    private String companyName;

    // this @OneToOne Annotation will create a new column in named citizen_id in Aadhar table to manage the relation.
    /* @JsonBackReference is used to manage the bidirectional relationship and it save from infinite recursion. This
   without this proerty it was calling infinite loop, this property ensured that Citizen will get aadhar data and Aadhar will not call back citizen data.
     */

    @OneToOne
    @JsonBackReference
    Citizen citizen;
}
