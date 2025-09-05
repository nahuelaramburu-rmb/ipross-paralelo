package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preautorizaciones")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescription implements Serializable {

    @EmbeddedId
    private GalbopPrescriptionEmbeddedId id;

    @Column(name = "IDPreAutorizacion", insertable = false, updatable = false)
    private Long prescriptionId;

    @Column(name = "IDObSoc")
    private Long funderId;

    @Column(name = "FechaInicio")
    private LocalDate dateFrom;

    @Column(name = "FechaVencimiento")
    private LocalDate dateTo;

    @Column(name = "CreadoFecha")
    private LocalDate createdAtDate;

    @Column(name = "CreadoHora")
    private LocalTime createdAtTime;

    @Column(name = "NroFormulario")
    private String applicationFormId;

    @Column(name = "Estado")
    private String status;

    @Column(name = "Origen")
    private String origin;

    @Column(name = "Autorizante")
    private String practitioner;

    @Column(name = "TipoProfesional")
    private String practitionerType;

    @Column(name = "TipoMatricula")
    private String registrationType;

    @Column(name = "ProvinciaMatricula")
    private String registrationProvince;

    @Column(name = "Diagnostico")
    private String disease;

    @Lob
    @Column(name = "Observacion")
    private byte[] observations;

    @Column(name = "IDProfesional")
    private Long registration;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "prescription")
    private Set<GalbopPrescriptionDetail> prescriptionDetails = new HashSet<>();

}
