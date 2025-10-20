package com.capacidad.validationapi.module.procedure.model;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "procedure_seq", allocationSize = 1)
@Relation(collectionRelation = "procedures")
public class UnknownAuthorizationProcedure extends Procedure {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_authorization_id")
    private MedicalAuthorization medicalAuthorization;

}
