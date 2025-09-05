package com.capacidad.validationapi.module.exportprocessor.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public interface ExportService<T extends BaseEntity<I>, I extends Serializable> {

    FileDownloadKey generateDownloadKey();

    void exportStreamToCsv(String search, String key, OutputStream outputStream) throws ObjectNotValidException;

    String[] toStringArray(Tuple tuple);

    List<Selection<?>> buildSelections(Root<T> root, CriteriaQuery<Tuple> query, CriteriaBuilder builder);

    Specification<T> buildSpecification(String search);

}
