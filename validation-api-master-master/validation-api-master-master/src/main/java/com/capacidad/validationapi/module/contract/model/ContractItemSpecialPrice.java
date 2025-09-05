package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.medicalcoverage.model.ChargeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "contract_item_special_price")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_item_special_price_seq", allocationSize = 1)
public class ContractItemSpecialPrice extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_item_id", nullable = false)
    private FixedContractItem contractItem;

    @Audited(withModifiedFlag = true)
    @Column(name = "special_value")
    private BigDecimal specialValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private CalendarEventType eventType;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_type_id", nullable = false)
    private ChargeType chargeType;

}
