package com.capacidad.validationapi.module.base.hateoas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;

public class PageModelAssembler<E extends EntityModel<?>> {

    public PageModelWrapper<E> toPageResource(String relation, CollectionModel<E> resources, Page<?> page, Pageable pageable) {
        Page<E> resourcePage = new PageImpl<>(new ArrayList<>(resources.getContent()), page.getPageable(), page.getTotalElements());
        return new PageModelWrapper<>(relation, resourcePage, pageable);
    }

    public PageModelWrapper<E> toPageResource(String relation, CollectionModel<E> resources, Page<?> page, Pageable pageable, MultiValueMap<String, String> queryParams) {
        Page<E> resourcePage = new PageImpl<>(new ArrayList<>(resources.getContent()), page.getPageable(), page.getTotalElements());
        return new PageModelWrapper<>(relation, resourcePage, pageable, queryParams);
    }

}
