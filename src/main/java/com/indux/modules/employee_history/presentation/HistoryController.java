package com.indux.modules.employee_history.presentation;

import com.indux.modules.employee_history.application.dto.FilterHistory;
import com.indux.modules.employee_history.application.dto.HistoryDTO;
import com.indux.modules.employee_history.application.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@Validated
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public Page<HistoryDTO> getHistory(
            Pageable pageable,
            FilterHistory filterHistory
    ) {
        return historyService.getHistory(filterHistory,pageable);
    }

    @GetMapping("/event")
    public Page<String> getEvents(
            Pageable pageable
    ){
        return historyService.getDistinctEvents(pageable);
    }
}
