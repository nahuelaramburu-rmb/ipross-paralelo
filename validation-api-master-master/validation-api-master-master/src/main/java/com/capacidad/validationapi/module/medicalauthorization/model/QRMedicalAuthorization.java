package com.capacidad.validationapi.module.medicalauthorization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_authorization_seq")
@Audited
@Relation(collectionRelation = "authorizations")
public class QRMedicalAuthorization extends MedicalAuthorization {

    @NotAudited
    @Column(name = "encrypted_qr_key")
    private String encryptedQrKey;

    @Transient
    private String type;

}
