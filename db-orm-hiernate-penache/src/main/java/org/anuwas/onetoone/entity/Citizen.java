package org.anuwas.onetoone.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    private String citizenName;
    private String gender;


    /* @oneToOne Annotation create column in table, but if we use mappedBy then hibernate will not crate a column inside table.
    Here no column will be created in Citazen table for Aadhar, but citizen will still mange the dependecy.
    Here mappedBy = "citizen" , this citizen is copied from Aadhar class under OneToOne mapping section
    */
    /*
    JsonManagedReference reference and JsonBackReference are used to manage the bidirectional relationship and it save from infinite recursion.
     */
    /*
    If we wants to save Citizan with Aadhar at a same time with single request
    then we need to use cascade = CascadeType.All. For saperate save this is not require.
    */

    @OneToOne(mappedBy = "citizen",fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    Aadhar aadhar;

}
