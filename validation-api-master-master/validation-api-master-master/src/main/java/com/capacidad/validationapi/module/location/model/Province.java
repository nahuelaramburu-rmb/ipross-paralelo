package com.capacidad.validationapi.module.location.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "country_id"}),
})
@NoArgsConstructor
@Getter
@Setter
public class Province extends BaseEntity<Long> {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "province", fetch = FetchType.LAZY)
    private Set<City> cities = new HashSet<>();

}
