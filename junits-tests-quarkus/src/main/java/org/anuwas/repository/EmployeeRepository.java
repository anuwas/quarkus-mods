package org.anuwas.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.anuwas.dto.EmployeeDTO;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmployeeRepository {
    public List<EmployeeDTO> getEmplyoeeList() {
        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
        employeeDTOList.add(new EmployeeDTO(1L, "F1", "L1"));
        employeeDTOList.add(new EmployeeDTO(2L, "F2", "L2"));
        return employeeDTOList;
    }

    public EmployeeDTO getEmployeeById(Long id) {
        // Implementation to get employee by ID
        return new EmployeeDTO(id, "FirstName" + id, "LastName" + id);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Implementation to create a new employee
        return employeeDTO; // In a real implementation, you would save this to a database and return the saved entity
    }
}
