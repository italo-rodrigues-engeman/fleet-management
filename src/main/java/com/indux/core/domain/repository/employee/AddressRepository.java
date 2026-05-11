package com.indux.core.domain.repository.employee;

import com.indux.core.domain.model.employee.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByRegistration(String registrationNo);
}
