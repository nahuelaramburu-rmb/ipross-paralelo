package com.capacidad.validationapi.module.storage.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MultipartFileListWrapper {

    private long relatedId;
    private List<MultipartFile> files;

    public MultipartFileListWrapper(List<MultipartFile> files, long relatedId) {
        this.relatedId = relatedId;
        this.files = files;
    }

}
