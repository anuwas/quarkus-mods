package org.anuwas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {

    // getters and setters for id, firstName, lastName
    private Long id;
    private String firstName;
    private String lastName;

}
