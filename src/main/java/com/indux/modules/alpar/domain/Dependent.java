package com.indux.modules.alpar.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dependent {

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("nome")
    private String name;

    @JsonProperty("sexo")
    @JsonDeserialize(using = Dependent.SexDeserializer.class)
    private String sex;

    @JsonProperty("nome_mae")
    @JsonDeserialize(using = Dependent.NullOrEmptyDeserializer.class)
    private String motherName;

    @JsonProperty("parentesco")
    private String kinship;

    @JsonProperty("estado_civil")
    private String maritalStatus;

    @JsonProperty("data_nascimento")
    private LocalDate birthDate;

    static class SexDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getText();
            if ("F".equalsIgnoreCase(value))
                return "Feminino";
            if ("M".equalsIgnoreCase(value))
                return "Masculino";
            return "Não informado";
        }
    }

    static class NullOrEmptyDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getText();
            return (value == null || value.isBlank()) ? "Não informado" : value;
        }

        @Override
        public String getNullValue(DeserializationContext ctx) {
            return "Não informado";
        }
    }
}
