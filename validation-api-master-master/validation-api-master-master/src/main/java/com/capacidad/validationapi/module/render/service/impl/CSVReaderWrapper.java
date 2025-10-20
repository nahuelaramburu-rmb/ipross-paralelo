package com.capacidad.validationapi.module.render.service.impl;

import com.capacidad.validationapi.module.importprocessor.model.ImportProperties;
import com.capacidad.validationapi.module.render.service.CSVReader;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.processor.ConcurrentRowProcessor;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;

@Log4j2
public class CSVReaderWrapper<T> implements CSVReader<T> {

    private final ImportProperties properties;
    private final Reader reader;
    private final InputStream inputStream;

    public CSVReaderWrapper(ImportProperties properties) {
        inputStream = getInputStream(properties.getMultipartFile());
        this.properties = properties;
        reader = properties.getReader();
    }

    private InputStream getInputStream(MultipartFile multipartFile) {
        try {
            return multipartFile.getInputStream();
        } catch (IOException e) {
            log.error("({}) - CSVReaderWrapper - getInputStream: {}", this.getClass(), e.getMessage());
            return null;
        }
    }

    @Override
    public Iterator<T> iterator(Class<T> entityClazz, boolean concurrent) {
        BeanListProcessor<T> rowProcessor = new BeanListProcessor<>(entityClazz);
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaders(properties.getColumns().toArray(new String[0]));
        settings.setHeaderExtractionEnabled(false);
        settings.getFormat().setDelimiter(properties.getSeparator());
        settings.setNumberOfRowsToSkip(properties.getSkipLines());
        settings.setProcessor(concurrent ? new ConcurrentRowProcessor(rowProcessor) : rowProcessor);
        return new CsvRoutines(settings).iterate(entityClazz, reader).iterator();
    }

    @Override
    public long getRowCount(Charset charset) {
        if (inputStream == null)
            return 0;
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(properties.getSeparator());
        settings.setNumberOfRowsToSkip(properties.getSkipLines());
        return new CsvRoutines(settings).getInputDimension(inputStream, charset.name()).rowCount();
    }

}
