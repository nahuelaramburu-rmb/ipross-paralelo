package com.capacidad.validationapi.module.location.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Entity
@Table(name = "address")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "address_seq", allocationSize = 1)
public class Address extends BaseTenantEntity<Long> {

    @Audited(withModifiedFlag = true)
    @Column
    private String district;

    @Audited(withModifiedFlag = true)
    @Column
    private String street;

    @Audited(withModifiedFlag = true)
    @Column(name = "street_number")
    private Integer streetNumber;

    @Audited(withModifiedFlag = true)
    @Column
    private String apartment;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

}
