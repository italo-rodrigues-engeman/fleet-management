package com.indux.modules.modulo_mega.infrastructure.converter; 

import com.indux.modules.modulo_mega.domain.enums.AbcClassificationCriteria;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

@Component
public class StringToAbcCriteriaConverter implements Converter<String, AbcClassificationCriteria> {

    @Override
    public AbcClassificationCriteria convert(@NonNull String source) {
        
        return AbcClassificationCriteria.fromString(source);
    }
}