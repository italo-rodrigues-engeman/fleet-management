package com.indux.core.domain.model.modules;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_acoes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ActionModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acao_id")
    private Long actionID;
    @Column(name = "nome")
    private String name;

    public enum Values {
        TODAS(0L),
        VISUALIZAR(1L),
        EDITAR(2L),
        EXCLUIR(3L);

        long actionID;

        Values(long roleID) {
            this.actionID = actionID;
        }

        public long getRoleId() {
            return actionID;
        }
    }
}
