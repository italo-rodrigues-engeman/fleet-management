package com.indux.modules.fleet_management.application.fleet_management.vehicle.usecase;

import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.CreateVehicleRequest;
import com.indux.modules.fleet_management.application.fleet_management.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportVehiclesUseCase {

    private final CreateVehicleUseCase createVehicleUseCase;

    public List<VehicleResponse> execute(MultipartFile file) {
        List<VehicleResponse> importedVehicles = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            System.out.println("Arquivo recebido: " + file.getOriginalFilename());
            System.out.println("Nome da aba: " + sheet.getSheetName());
            System.out.println("Última linha: " + sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                System.out.println("Lendo linha: " + i);

                if (row == null) {
                    System.out.println("Linha " + i + " está nula");
                    continue;
                }

                String plate = getCellValue(row.getCell(0));

                System.out.println("Linha " + i + " plate: [" + plate + "]");

                if (plate == null || plate.isBlank()) {
                    System.out.println("Linha " + i + " ignorada por placa vazia");
                    continue;
                }

                CreateVehicleRequest request = new CreateVehicleRequest(
                        plate,
                        getCellValue(row.getCell(1)),
                        getCellValue(row.getCell(2)),
                        toInteger(getCellValue(row.getCell(3))),
                        toInteger(getCellValue(row.getCell(4))),
                        getCellValue(row.getCell(5)),
                        getCellValue(row.getCell(6)),
                        getCellValue(row.getCell(7)),
                        getCellValue(row.getCell(8)),
                        getCellValue(row.getCell(9)),
                        getCellValue(row.getCell(10)),
                        getCellValue(row.getCell(11)),
                        toBoolean(getCellValue(row.getCell(12))),
                        toBoolean(getCellValue(row.getCell(13))),
                        getCellValue(row.getCell(15)),
                        getCellValue(row.getCell(16))
                );

                VehicleResponse response = createVehicleUseCase.execute(request);
                importedVehicles.add(response);

                System.out.println("Veículo importado: " + plate);
            }

            System.out.println("Total importado: " + importedVehicles.size());

            return importedVehicles;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar planilha de veículos: " + e.getMessage(), e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Integer toInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Integer.valueOf(value);
    }

    private Boolean toBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        return normalized.equals("true")
                || normalized.equals("sim")
                || normalized.equals("ativo")
                || normalized.equals("active")
                || normalized.equals("1");
    }
}