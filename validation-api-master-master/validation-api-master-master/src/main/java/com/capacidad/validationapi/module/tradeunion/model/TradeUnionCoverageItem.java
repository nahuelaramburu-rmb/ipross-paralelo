package com.capacidad.validationapi.module.tradeunion.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "trade_union_coverage_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nomenclator_id", "trade_union_id", "deleted", "deletion_token", "tenant_id"}),
})
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "trade_union_coverage_item_seq", allocationSize = 1)
@Relation(collectionRelation = "tradeUnionCoverageItems")
public class TradeUnionCoverageItem extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_union_id", nullable = false)
    private TradeUnion tradeUnion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false)
    private Nomenclator nomenclator;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private Integer quantity;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private BigDecimal percentage;

}
