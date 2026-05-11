package com.indux.core.infra.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParcialUpdate {
    public static <T> void updateFieldIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    public static <S, T> void updateListIfNotNull(List<S> sourceList, Function<S, T> mapper, Consumer<List<T>> listSetter) {
        if (sourceList != null) {
            listSetter.accept(sourceList.stream().map(mapper).toList());
        }
    }

}
