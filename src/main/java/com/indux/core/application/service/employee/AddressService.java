package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.Address;
import com.indux.core.domain.repository.employee.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address getAddressByRegistrationNo(String registrationNo) {
        List<Address> addresses = addressRepository.findByRegistration(registrationNo);
        return addresses.stream()
                .filter(a -> a.getDate() != null)
                .max(Comparator.comparing(Address::getDate))
                .orElse(null);
    }
}
