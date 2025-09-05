package com.capacidad.validationapi.module.render.service;

import java.nio.charset.Charset;
import java.util.Iterator;

public interface CSVReader<T> {

    Iterator<T> iterator(Class<T> entityClazz, boolean concurrent);

    long getRowCount(Charset charset);

}
