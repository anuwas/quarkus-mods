package org.anuwas.dto;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {

  // declar id , firstName
  private Long id;
  private String firstName;
  private String lastName;

}
