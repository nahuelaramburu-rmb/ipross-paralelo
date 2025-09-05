package com.capacidad.validationapi.module.ruleprocessor.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleData;

import java.util.Set;

public interface RuleConfigurationProjection extends BaseProjection<Long> {

    Set<RuleData> getData();

    Boolean getActive();

    Boolean getApplyToBatch();

    RuleProjection getRuleRef();

    IdAndNameOnlyProjection getRestrictionType();

    IdAndNameOnlyProjection getContract();

}
