package com.capacidad.validationapi.module.render.service;

public interface CSVWriter extends AutoCloseable {

    void writeNext(String[] nextLine);

    void flush();

}
