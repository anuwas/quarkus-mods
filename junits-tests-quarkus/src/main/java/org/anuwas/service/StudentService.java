package org.anuwas.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.dto.StudentDTO;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class StudentService {

    public List<StudentDTO> getAllStudents() {
        // Logic to retrieve all students from the database
        // For demonstration, returning a static list
        List<StudentDTO> students = new ArrayList<>();
        StudentDTO student1 = new StudentDTO();
        student1.setId(1L);
        student1.setFirstName("F1");
        student1.setLastName("L1");
        students.add(student1);

        StudentDTO student2 = new StudentDTO();
        student2.setId(2L);
        student2.setFirstName("F2");
        student2.setLastName("L2");
        students.add(student2);

        StudentDTO student3 = new StudentDTO();
        student3.setId(3L);
        student3.setFirstName("F3");
        student3.setLastName("L3");
        students.add(student3);

        return students;

    }
}
