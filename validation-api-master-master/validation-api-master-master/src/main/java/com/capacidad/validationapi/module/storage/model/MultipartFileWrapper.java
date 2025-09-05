package com.capacidad.validationapi.module.storage.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
public class MultipartFileWrapper {

    private long relatedId;
    private MultipartFile file;

    public MultipartFileWrapper(MultipartFile file, long relatedId) {
        this.relatedId = relatedId;
        this.file = file;
    }

}
