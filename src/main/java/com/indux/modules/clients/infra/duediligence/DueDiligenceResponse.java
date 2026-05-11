package com.indux.modules.clients.infra.duediligence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DueDiligenceResponse {
    private String cnpj;
    private Boolean hasDiligence;
    private String filePath;

    public static DueDiligenceResponse fromJson(JSONObject json){
        return new DueDiligenceResponse(
                json.getString("cpf_do_cliente"),
                json.getBoolean("possue_due_diligente"),
                json.getString("caminho_certidao_negativa")
        );
    }

    public static DueDiligenceResponse fromBase64(String cnpj, String base64Content){
        return new DueDiligenceResponse(
                cnpj,
                true,
                base64Content
        );
    }
}
