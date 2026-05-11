package com.indux.modules.ppu.infra.mio;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface MioApiGateway {
    String authenticate() throws IOException;
    JsonNode fetchBoardedEmployeeData(String token, String initialDate, String finalDate) throws IOException;
    JsonNode fetchBoardedEmployeeWithStatus(String token, String initialDate, String finalDate) throws IOException;
}