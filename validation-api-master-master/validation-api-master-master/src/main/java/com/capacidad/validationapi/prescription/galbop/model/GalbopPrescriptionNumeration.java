package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "numeracionreceta")
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescriptionNumeration implements Serializable {

    @Id
    @Column(name = "numreceta")
    private Long numeration;

}
