package com.capacidad.validationapi.module.person.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "phone",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"area_code", "phone_number", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "phone_seq", allocationSize = 1)
public class Phone extends BaseTenantEntity<Long> {

    @Column(name = "area_code")
    private Integer areaCode;

    @Column(name = "phone_number", nullable = false)
    private Long phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhoneType phoneType;

}
