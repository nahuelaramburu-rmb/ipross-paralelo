package com.capacidad.validationapi.module.location.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "province_id"}),
})
@NoArgsConstructor
@Getter
@Setter
public class City extends BaseEntity<Long> {

    @Column(nullable = false)
    private String name;

    @Column(name = "postal_code")
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

}
