package com.indux.modules.flash_fuel.domain.repository;

import com.indux.modules.flash_fuel.domain.dtos.FlashFuelFilter;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFlashFuelRepository {
    Page<FlashFuel> findByFilter(FlashFuelFilter filter, Pageable pageable);

}
