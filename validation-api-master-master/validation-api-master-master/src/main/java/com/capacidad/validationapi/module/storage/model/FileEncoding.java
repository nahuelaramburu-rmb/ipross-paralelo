package com.capacidad.validationapi.module.storage.model;

public enum FileEncoding {
    PDF("application/pdf"),
    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PNG("image/png"),
    JPG("image/jpeg"),
    JPEG("image/jpeg"),
    TXT("text/plain");

    private final String encoding;

    FileEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

}
