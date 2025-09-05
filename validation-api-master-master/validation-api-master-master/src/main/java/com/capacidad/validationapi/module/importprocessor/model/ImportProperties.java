package com.capacidad.validationapi.module.importprocessor.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.io.Reader;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ImportProperties {

    private final int skipLines;
    private final char separator;
    private final List<String> columns;
    private final ImportOperation operation;
    private final MultipartFile multipartFile;
    private final OutputStream outputStream;
    private Reader reader;
    private Authentication authentication;
    private UUID tenantId;

    public ImportProperties(List<String> columns,
                            int skipLines,
                            Character separator,
                            ImportOperation operation,
                            MultipartFile multipartFile,
                            OutputStream outputStream,
                            Authentication authentication,
                            UUID tenantId) {
        this.columns = columns;
        this.skipLines = skipLines;
        this.separator = separator != null ? separator : ',';
        this.operation = operation;
        this.multipartFile = multipartFile;
        this.outputStream = outputStream;
        this.authentication = authentication;
        this.tenantId = tenantId;
    }

}
