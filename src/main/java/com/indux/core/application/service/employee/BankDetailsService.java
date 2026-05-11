package com.indux.core.application.service.employee;

import com.indux.core.application.dto.generic.BankDetailDTO;
import com.indux.core.domain.model.employee.BankDetails;
import com.indux.core.domain.repository.generic.BankDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankDetailsService {

    private final BankDetailsRepository bankDetailsRepository;

    public BankDetailsService(BankDetailsRepository bankDetailsRepository) {
        this.bankDetailsRepository = bankDetailsRepository;
    }

    public Optional<BankDetailDTO> getByRegistration(String registration) {
        return bankDetailsRepository.findFirstByMatricula(registration)
                .map(BankDetails::toDTO);
    }
}

