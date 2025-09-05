package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_item_seq")
@Audited
@Relation(collectionRelation = "contractItems")
public class FixedContractItem extends ContractItem {

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id")
    private Nomenclator nomenclator;

    @OneToMany(mappedBy = "contractItem")
    private Set<ContractItemSpecialPrice> specialPrices = new HashSet<>();

}
