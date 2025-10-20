package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "preautorizaciones")
@NoArgsConstructor
@Getter
@Setter
public class GalbopBeneficiary implements Serializable {

    @Id
    @EmbeddedId
    private GalbopBeneficiaryEmbeddedId id;

}
