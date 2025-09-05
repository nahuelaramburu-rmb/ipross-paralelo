package com.capacidad.validationapi.module.storage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SummaryDTO {

    private String name;

    private long size;

    private String fileType;

    private String tag;

}
