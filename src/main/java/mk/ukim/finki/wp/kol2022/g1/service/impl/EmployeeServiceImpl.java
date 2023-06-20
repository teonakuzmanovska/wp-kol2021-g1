package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import mk.ukim.finki.wp.kol2022.g1.service.SkillService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final SkillService skillService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SkillRepository skillRepository, PasswordEncoder passwordEncoder, SkillService skillService) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
        this.skillService = skillService;
    }

    @Override
    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = skillRepository.findAllById(skillId);
        Employee employee = new Employee(name, email, password, type, skills, employmentDate);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        Employee employee = this.findById(id);
        employee.setName(name);
        employee.setEmail(email);
        String encodedPassword = passwordEncoder.encode(password);
        employee.setPassword(encodedPassword);
        employee.setType(type);
        List<Skill> skills = skillRepository.findAllById(skillId);
        employee.setSkills(skills);
        employee.setEmploymentDate(employmentDate);
        return this.employeeRepository.save(employee);
    }

    @Override
    public Employee delete(Long id) {
        Employee employee = this.findById(id);
        this.employeeRepository.delete(employee);
        return employee;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
//        TODO: implement this

        if(skillId != null && yearsOfService != null) {
            Skill skill = skillService.findById(skillId);
            LocalDate localDate = LocalDate.now().minusYears(yearsOfService);

            return this.employeeRepository.findEmployeeByEmploymentDateBeforeAndSkillsContaining(localDate, skill);
        }
        else if (skillId != null) {
            Skill skill = skillService.findById(skillId);
            return this.employeeRepository.findEmployeeBySkillsContaining(skill);
        }
        else if (yearsOfService != null) {
            LocalDate localDate = LocalDate.now().minusYears(yearsOfService);
            return this.employeeRepository.findEmployeeByEmploymentDateBefore(localDate);
        }
        else {
            return employeeRepository.findAll();
        }
    }
}
