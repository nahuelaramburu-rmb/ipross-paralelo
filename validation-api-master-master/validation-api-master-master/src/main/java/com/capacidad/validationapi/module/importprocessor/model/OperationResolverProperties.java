package com.capacidad.validationapi.module.importprocessor.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Writer;
import java.util.List;

@Getter
@Setter
public class OperationResolverProperties<T> {

    private List<T> objects;
    private ImportProperties importProperties;
    private Writer writer;
    private long totalElements;
    private int batchSize;

    public OperationResolverProperties(List<T> objects, ImportProperties importProperties, Writer writer, long totalElements, int batchSize) {
        this.objects = objects;
        this.importProperties = importProperties;
        this.batchSize = batchSize;
        this.writer = writer;
        this.totalElements = totalElements;
    }

}
