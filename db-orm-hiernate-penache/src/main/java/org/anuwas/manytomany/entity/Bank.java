package org.anuwas.manytomany.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    private String bankName;
    private String brnch;
    private String ifsc;

    /*
     If we dont declare mapped by here, the hibernate will create two mapping table
     one for dweller_bank and another bank_dweller. But we need only one mapping table, this
     is why here mapped by decleard to stop creating another  mapping table.
     */
    @ManyToMany(mappedBy = "bankList")
    @JsonBackReference
    private List<Dweller> dwellerList;


}
