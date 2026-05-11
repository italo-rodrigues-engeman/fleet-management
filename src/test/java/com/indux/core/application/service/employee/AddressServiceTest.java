package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.Address;
import com.indux.core.domain.repository.employee.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

        @Mock
        private AddressRepository addressRepository;

        @InjectMocks
        private AddressService addressService;

        private Address addressOld;
        private Address addressNew;
        private Address addressNullDate;

        @BeforeEach
        void setUp() {
                Calendar cal1 = Calendar.getInstance();
                cal1.set(2020, Calendar.JANUARY, 1);
                addressOld = Address.builder()
                                .date(cal1.getTime())
                                .street("Rua A")
                                .build();

                Calendar cal2 = Calendar.getInstance();
                cal2.set(2023, Calendar.MAY, 10);
                addressNew = Address.builder()
                                .date(cal2.getTime())
                                .street("Rua B")
                                .build();

                addressNullDate = Address.builder()
                                .date(null)
                                .street("Rua C")
                                .build();
        }

        @Test
        @DisplayName("Should return address with max date")
        void shouldReturnAddressWithMaxDate() {
                when(addressRepository.findByRegistration(anyString()))
                                .thenReturn(List.of(addressOld, addressNew, addressNullDate));

                Address result = addressService.getAddressByRegistrationNo("123");

                assertNotNull(result);
                assertEquals(addressNew.getStreet(), result.getStreet());
                assertEquals(addressNew.getDate(), result.getDate());
        }

        @Test
        @DisplayName("Should ignore null dates and return valid max date")
        void shouldIgnoreNullDatesAndReturnValidMaxDate() {
                when(addressRepository.findByRegistration(anyString()))
                                .thenReturn(List.of(addressOld, addressNullDate));

                Address result = addressService.getAddressByRegistrationNo("123");

                assertNotNull(result);
                assertEquals(addressOld.getStreet(), result.getStreet());
                assertEquals(addressOld.getDate(), result.getDate());
        }

        @Test
        @DisplayName("Should return null when list is empty")
        void shouldReturnNullWhenListIsEmpty() {
                when(addressRepository.findByRegistration(anyString()))
                                .thenReturn(Collections.emptyList());

                Address result = addressService.getAddressByRegistrationNo("123");

                assertNull(result);
        }

        @Test
        @DisplayName("Should return null when all dates are null")
        void shouldReturnNullWhenAllDatesAreNull() {
                when(addressRepository.findByRegistration(anyString()))
                                .thenReturn(List.of(addressNullDate, addressNullDate));

                Address result = addressService.getAddressByRegistrationNo("123");

                assertNull(result);
        }
}
