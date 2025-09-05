package com.capacidad.validationapi.module.ruleprocessor.projection;

public interface RulePropertyMetadataProjection {

    String getType();


    String getDescription();


    String getDataIdentifier();


    String getLabelKey();


    String getDataKey();


    String getDataKeyWrapper();


    Boolean getRequired();


    String getDataType();

}
