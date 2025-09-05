package com.capacidad.validationapi.module.ruleprocessor.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.util.List;

public interface RuleProjection extends BaseProjection<Long> {

    String getName();

    String getDescription();

    String getRuleType();

    List<RulePropertyMetadataProjection> getMetadata();

}
