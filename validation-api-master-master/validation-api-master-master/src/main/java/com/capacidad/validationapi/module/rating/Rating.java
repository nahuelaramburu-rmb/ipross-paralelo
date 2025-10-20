package com.capacidad.validationapi.module.rating;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "rating")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "rating_seq", allocationSize = 1)
public class Rating extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal average;

    @Column(nullable = false)
    private BigDecimal quality;

    @Column(nullable = false)
    private BigDecimal duration;

    @Column(nullable = false)
    private BigDecimal charges;

    @Column(name = "wait_time", nullable = false)
    private BigDecimal waitTime;

}
