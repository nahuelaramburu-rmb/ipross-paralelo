package com.capacidad.validationapi.module.procedure.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "procedure_seq", allocationSize = 1)
@Relation(collectionRelation = "procedures")
public class DisabilityProcedure extends Procedure {
}
