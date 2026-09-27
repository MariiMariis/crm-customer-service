package com.pb.crm.sales.domain.activity;

public record RelatedTo(RelatedType type, Long id) {

    public RelatedTo {
        if (type == null) {
            throw new IllegalArgumentException("tipo do registro relacionado e obrigatorio");
        }
        if (id == null) {
            throw new IllegalArgumentException("id do registro relacionado e obrigatorio");
        }
    }
}
