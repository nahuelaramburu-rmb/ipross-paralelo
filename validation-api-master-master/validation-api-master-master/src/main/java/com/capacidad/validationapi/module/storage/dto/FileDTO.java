package com.capacidad.validationapi.module.storage.dto;

import com.capacidad.validationapi.module.storage.model.FileEncoding;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class FileDTO {

    @NotNull
    @Size(max = 2000000)
    private byte[] file;

    @NotNull
    private Long relatedId;

    @Size(min = 1, max = 50)
    private String filename;

    private FileEncoding fileEncoding;

}
