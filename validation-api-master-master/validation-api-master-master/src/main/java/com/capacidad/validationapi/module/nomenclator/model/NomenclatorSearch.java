package com.capacidad.validationapi.module.nomenclator.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class NomenclatorSearch {

    private NomenclatorSearchType type;

    private Object content;

}
