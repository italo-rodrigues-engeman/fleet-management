package com.indux.modules.ppu.infra.mio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.indux.modules.ppu.infra.mio.dto.MioResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Cacheable("boardedEmployees")
@Component
public class EmployeeBoardingETL {

    private final GetEmployeeUseCase getEmployee;
    private final MioApiGateway mioApi;
    private final ObjectMapper mapper;

    public EmployeeBoardingETL(GetEmployeeUseCase getEmployee, MioApiGateway mioApi) {
        this.getEmployee = getEmployee;
        this.mioApi = mioApi;
        this.mapper = new ObjectMapper();
    }

    @Cacheable(value = "boardedEmployees", key = "#initialDate + '-' + #finalDate")
    public List<BoardedEmployee> fetch(String initialDate, String finalDate) throws IOException {
        validateDates(initialDate, finalDate);

        Result result = fetchMioData(initialDate, finalDate);

        try {
            return addStatusInBoardedEmployee(result.fetchEmployees().get(), result.fetchEmployeeStatus().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Erro ao combinar status dos colaboradores embarcados", e);
        }
    }

    @NotNull
    private Result fetchMioData(String initialDate, String finalDate) {
        CompletableFuture<List<BoardedEmployee>> fetchEmployees = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchBoardedEmployeesForDate(initialDate, finalDate);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<List<MioResponse>> fetchEmployeeStatus = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchBoardedEmployeesStatus(initialDate, finalDate);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture.allOf(fetchEmployees, fetchEmployeeStatus).join();
        return new Result(fetchEmployees, fetchEmployeeStatus);
    }


    public List<BoardedEmployee> fetchBoardedEmployeesForDate(String start, String end) throws IOException {
        return fetchBoardedEmployees(null, null, start, end);
    }

    public List<BoardedEmployee> fetchBoardedEmployees(String registration, String platform,
                                                       String initialDate, String finalDate) throws IOException {
        validateDates(initialDate, finalDate);

        JsonNode dataNode = mioApi.fetchBoardedEmployeeData(null, initialDate, finalDate);

        if (dataNode == null || !dataNode.isArray()) {
            throw new ModuleFailure("Resposta da API MIO malformada.");
        }

        List<Map<String, Object>> records = mapper.convertValue(dataNode, new TypeReference<>() {});
        return filterByCriteria(records, registration, platform);
    }

    public List<MioResponse> fetchBoardedEmployeesStatus(String initialDate, String finalDate) throws IOException {
        validateDates(initialDate, finalDate);
        String token = mioApi.authenticate();
        JsonNode dataNode = mioApi.fetchBoardedEmployeeWithStatus(token, initialDate, finalDate);
        if (!dataNode.isArray()) {
            throw new ModuleFailure("Resposta da API MIO malformada.");
        }
        List<Map<String, Object>> records = mapper.convertValue(dataNode, new TypeReference<>() {
        });
        return records.stream().map(MioResponse::fromJson).toList();
    }


    private List<BoardedEmployee> addStatusInBoardedEmployee(List<BoardedEmployee> boarded,
                                                             List<MioResponse> responses) {

        Map<String, MioResponse> responseMap = responses.stream()
                .collect(Collectors.groupingBy(
                        MioResponse::matricula,
                        Collectors.collectingAndThen(
                                Collectors.minBy(PRIORITY_COMPARATOR),
                                opt -> opt.orElse(null)
                        )
                ));

        return boarded.stream()
                .peek(be -> {
                    MioResponse response = responseMap.get(be.getRegistration());
                    if (response != null) {
                        be.setStatus(response.status());
                    }
                })
                .toList();
    }

    public List<BoardedEmployee> resolveSimpleEmployeesFromBoardedEmployees(List<BoardedEmployee> boardedEmployees) {
        List<String> registrations = boardedEmployees.stream()
                .map(BoardedEmployee::getRegistration)
                .toList();

        List<SimpleEmployeeDTO> employees = getEmployee.findByRegistrations(registrations);

        Map<String, SimpleEmployeeDTO> employeeMap = employees.stream()
                .collect(Collectors.toMap(
                        SimpleEmployeeDTO::getRegistration,
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        return boardedEmployees.stream()
                .map(be -> Optional.ofNullable(employeeMap.get(be.getRegistration()))
                        .map(dto -> BoardedEmployee.fromSimpleEmployee(dto, be))
                        .orElse(be))
                .toList();
    }

    public List<BoardedEmployee> filterSimpleEmployeesByPosition(List<BoardedEmployee> dtos, List<String> positions) {
        var set = new HashSet<>(positions);
        return dtos.stream()
                .filter(dto -> set.contains(dto.getPosition()))
                .toList();
    }

    public static String getDefaultDate() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE); //D-1
    }

    private List<BoardedEmployee> filterByCriteria(List<Map<String, Object>> records, String registration, String platform) {
        return records.stream()
                .filter(r -> registration == null || registration.equals(r.get("Matrícula")))
                .filter(r -> platform == null || platform.equals(r.get("Destino")))
                .map(BoardedEmployee::fromJson)
                .collect(Collectors.toList());
    }

    private void validateDates(String initialDate, String finalDate) {
        if (!StringUtils.hasText(initialDate) || !StringUtils.hasText(finalDate)) {
            throw new IllegalArgumentException("Parâmetros obrigatórios ausentes.");
        }
    }

    private record Result(CompletableFuture<List<BoardedEmployee>> fetchEmployees,
                          CompletableFuture<List<MioResponse>> fetchEmployeeStatus) {
    }

    private final Comparator<MioResponse> PRIORITY_COMPARATOR = (a, b) -> {
        BigDecimal ordemA = parseOrdem(a.ordem());
        BigDecimal ordemB = parseOrdem(b.ordem());

        boolean aNeg = ordemA.signum() < 0;
        boolean bNeg = ordemB.signum() < 0;

        if (aNeg && !bNeg) return -1;
        if (!aNeg && bNeg) return 1;

        return ordemA.compareTo(ordemB);
    };

    private BigDecimal parseOrdem(String ordem) {
        if (ordem == null || ordem.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(ordem);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }


}