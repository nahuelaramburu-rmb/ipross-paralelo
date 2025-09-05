package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.organization.model.Organization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_seq", allocationSize = 1)
@Audited
@Relation(collectionRelation = "contracts")
public class OrganizationContract extends Contract {

    @NotAudited
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

}
