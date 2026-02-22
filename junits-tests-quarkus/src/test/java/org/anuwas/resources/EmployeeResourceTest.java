package org.anuwas.resources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.anuwas.dto.EmployeeDTO;
import org.anuwas.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EmployeeResourceTest {

    @InjectMock
    EmployeeRepository employeeRepository;

    @Inject
    EmployeeResource employeeResource;

    @BeforeEach
    void setUp() {
    }

    // generate test for showAllEmployees method
    @Test
    void testShowAllEmployees() {

        // given
        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
        employeeDTOList.add(new EmployeeDTO(1L, "F1", "L1"));
        employeeDTOList.add(new EmployeeDTO(2L, "F2", "L2"));
        // when
        Mockito.when(employeeRepository.getEmplyoeeList()).thenReturn(employeeDTOList);

        Response response = employeeResource.showAllEmployees();
        // then
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertNotNull(response);
    }

    // generate test for getEmployeeById method
    @Test
    void testGetEmployeeById() {
        // given
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "F1", "L1");
        // when
        /* this is dynamic approach */
        Mockito.when(employeeRepository.getEmployeeById(ArgumentMatchers.any(Long.class))).thenReturn(employeeDTO);

        /* this is static apprach */
        // Mockito.when(employeeRepository.getEmployeeById(1L)).thenReturn(employeeDTO);

        Response response = employeeResource.getEmployeeById(2L);
        // then
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertNotNull(response);

        /*
        * to test the negative scenarion test like this
        * assertEquals(404, response.Status.NO_CONTENT.getStatusCode());
        * */
    }

    // generate test for createEmployee method
    @Test
    void testCreateEmployee() {
        // given
        EmployeeDTO employeeDTO = new EmployeeDTO(1L, "F1", "L1");
        // when
        /* this can be tes for persist method, mean when persist called the do nothting method will be called */
       // Mockito.doNothing().when(employeeRepository).createEmployee(employeeDTO);

        /* but whe we will called isPersist then we will return the employeeDTO */
        //Mockito.when(employeeRepository.isPersist(employeeDTO)).thenReturn(true); - it will return employeeDTO for ture

        Response response = employeeResource.createEmployee(employeeDTO);
        // then
        assertEquals(200, response.getStatus());


    }

}