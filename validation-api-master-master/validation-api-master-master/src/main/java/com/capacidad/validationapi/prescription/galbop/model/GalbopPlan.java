package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "planes")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPlan implements Serializable {

    @EmbeddedId
    private GalbopPlanEmbeddedId id;

    @Column(name = "CtrlTam")
    private String quantityRules;

}
