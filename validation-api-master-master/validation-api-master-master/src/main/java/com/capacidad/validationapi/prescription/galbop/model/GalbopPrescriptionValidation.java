package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "preautorizacionesvalidacion")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescriptionValidation implements Serializable {

    @EmbeddedId
    private GalbopPrescriptionValidationEmbeddedId id;

    @Column(name = "IDDroga")
    private Long drugId;

    @Column(name = "IDForma")
    private Long productTypeId;

    @Column(name = "Concentracion")
    private BigDecimal concentration;

    @Column(name = "IDTipoConc")
    private Long concentrationTypeId;

    @Column(name = "IDTipoUnidad")
    private Long unitsTypeId;

    @Column(name = "ContEnvase")
    private Long units;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPreAutorizacion", referencedColumnName = "IDPreAutorizacion", insertable = false, updatable = false)
    @JoinColumn(name = "Receta", referencedColumnName = "Receta", insertable = false, updatable = false)
    @JoinColumn(name = "Orden", referencedColumnName = "Orden", insertable = false, updatable = false)
    private GalbopPrescriptionLine prescriptionLine;

}
