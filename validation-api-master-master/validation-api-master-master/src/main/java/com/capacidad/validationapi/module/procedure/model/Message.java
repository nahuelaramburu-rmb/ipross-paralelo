package com.capacidad.validationapi.module.procedure.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class Message implements Serializable {

    private String from;

    private String text;

    private LocalDateTime sentAt;

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Message))
            return false;
        Message other = (Message) o;
        return StringUtils.equals(this.getFrom(), other.getFrom()) &&
                StringUtils.equals(this.getText(), other.getText()) &&
                this.getSentAt().isEqual(other.getSentAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, text, sentAt);
    }


}
