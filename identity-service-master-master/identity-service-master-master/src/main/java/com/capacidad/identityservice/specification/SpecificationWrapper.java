package com.capacidad.identityservice.specification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
@Getter
@Setter
public class SpecificationWrapper<T> {

    private Specification<T> specification;

    private String search;

    private String key;

    public SpecificationWrapper(Specification<T> specification, String search, String key) {
        this.specification = specification;
        this.search = search;
        this.key = key;
    }

}
