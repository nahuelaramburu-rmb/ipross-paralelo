package com.capacidad.validationapi.module.procedure.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class FileTag implements Serializable {

    private String filename;

    private String tag;

}
