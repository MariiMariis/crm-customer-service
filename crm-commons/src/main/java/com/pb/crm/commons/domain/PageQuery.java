package com.pb.crm.commons.domain;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("a pagina deve ser maior ou igual a zero");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("o tamanho da pagina deve estar entre 1 e " + MAX_SIZE);
        }
    }
}
