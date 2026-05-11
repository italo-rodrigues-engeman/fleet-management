package com.indux.modules.modulo_mega.domain.enums;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.indux.modules.modulo_mega.domain.repository.specs.MegaItemQueries;

import java.util.Arrays;

public enum AutocompleteStrategy {
    FORNECEDOR("fornecedor", "Fornecedor") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findSuppliers(term, pageable);
        }
    },
    
    STATUS_ITEM("status_item", "Status do Item") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            
            return queries.findDistinctStatuses(term, pageable); 
        }
    },
    COMPRADOR("comprador", "Comprador") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findBuyer(term, pageable);
        }
    },
    REGIONAL("regional", "Regional") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findRegional(term, pageable);
        }
    },

    REGIONAL_CODIGO("cod_regional", "Código da Regional") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findRegionalCode(term, pageable);
        }
    },

    SUPERINTENDENCIA("superintendencia", "Nome da Superintendência") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findSuper(term, pageable);
        }
    },

    SUPERINTENDENCIA_CODIGO("cod_superintendencia", "Código da Superintendência") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findSuperCode(term, pageable);
        }
    },

    DIRETORIA("diretoria", "Nome da Diretoria") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findDirectory(term, pageable);
        }
    },

    DIRETORIA_CODIGO("cod_diretoria", "Código da Diretoria") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findDirectoryCode(term, pageable);
        }
    },

    SOLICITANTE("solicitante", "Solicitante") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findRequester(term, pageable);
        }
    },
    PROJETO("projeto", "Nome do Projeto") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findProject(term, pageable);
        }
    },

    PROJETO_CODIGO("cod_projeto", "Código do Projeto") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findProjectCode(term, pageable);
        }
    },
    FILIAL_MEGA("filial_mega", "Filial Mega") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findBranchMega(term, pageable);
        }
    },
    CONTRATO("contrato", "Nome do Contrato") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findContract(term, pageable);
        }
    },

    CONTRATO_CODIGO("cod_contrato", "Código do Contrato") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findContractCode(term, pageable);
        }
    },

    SETOR_CODIGO("cod_setor", "Código do Setor") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findSectorCode(term, pageable);
        }
    },

    SETOR("setor", "Nome do Setor") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findSector(term, pageable);
        }
    },

    COD_ITEM("cod_item", "Código do item") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findItensCode(term, pageable);
        }
    },

//    NOME_ITEM("item", "Nome do item") {
//        @Override
//        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
//            return queries.findItens(term, pageable);
//        }
//    },

    UNIDADE_MEDIDA("unidade_medida", "Unidade de Medida") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findUnitOfMeasure(term, pageable);
        }
    },
    STATUS_PEDIDO("status_pedido", "Pedido Atendido, Pedido Cancelado...") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findOrderStatus(term, pageable);
        }
    },
    TIPO_ITEM("tipo_item", "Produto ou Serviço") {
        @Override
        public Page<?> search(MegaItemQueries queries, String term, Pageable pageable) {
            return queries.findItemType(term, pageable);
        }
    };

    private final String value;
    private final String label;

    AutocompleteStrategy(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    // Assinatura atualizada para retornar Page<?>
    public abstract Page<?> search(MegaItemQueries queries, String term, Pageable pageable);

    public static AutocompleteStrategy fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tipo de autocomplete não pode estar vazio");
        }

        for (AutocompleteStrategy type : AutocompleteStrategy.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo inválido: " + value + ". Tipos disponíveis: " +
                String.join(", ", Arrays.stream(AutocompleteStrategy.values())
                        .map(t -> t.value).toArray(String[]::new)));
    }
}