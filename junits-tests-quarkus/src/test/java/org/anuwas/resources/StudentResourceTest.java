package org.anuwas.resources;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.anuwas.dto.StudentDTO;
import org.anuwas.service.StudentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StudentResourceTest {

@Inject
StudentService studentService;

    // generate test case for getAllStudents method
    @Test
    void testGetAllStudents() {
        given()
                .when().get("/students/all")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].firstName", equalTo("F1"))
                .body("[0].lastName", equalTo("L1"))
                .body("[1].firstName", equalTo("F2"))
                .body("[1].lastName", equalTo("L2"))
                .body("[2].firstName", equalTo("F3"))
                .body("[2].lastName", equalTo("L3"));
    }

    @Test
    void testAllStudentList() {
        List<StudentDTO> studentDTOList = given()
                .when().get("/students/all")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", StudentDTO.class);
    }

    @Test
    void testAllStudentAssert() {
        List<StudentDTO> studentDTOList = studentService.getAllStudents();
        assertFalse(studentDTOList.isEmpty());
        assertNotNull(studentDTOList);
        assertEquals(3, studentDTOList.size());
        assertEquals("F1", studentDTOList.get(0).getFirstName());
        assertEquals("L1", studentDTOList.get(0).getLastName());
    }
}