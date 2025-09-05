package com.capacidad.validationapi.module.ruleprocessor.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class RuleData implements Serializable {

    private JsonNode content;

    private String dataIdentifier;

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RuleData))
            return false;
        RuleData other = (RuleData) o;
        return StringUtils.equals(this.getDataIdentifier(), other.getDataIdentifier()) &&
                this.getContent().equals(other.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, dataIdentifier);
    }

}
