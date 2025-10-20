package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@NoArgsConstructor
@Getter
@Setter
public class GalbopProduct implements Serializable {

    @Id
    @Column(name = "IDProducto")
    private Long id;

    @Column(name = "Producto")
    private String product;

    @Column(name = "Presentacion")
    private String presentation;

    @Column(name = "Unidades")
    private Integer units;

    @Column(name = "Concentracion")
    private BigDecimal concentration;

    @Column(name = "IDTipoConc")
    private Long concentrationTypeId;

    @Column(name = "IDForma")
    private Long productTypeId;

    @Column(name = "IDTipoUnidad")
    private Long unitsTypeId;

    @Column(name = "IDDroga")
    private Long drugId;

    @Column(name = "IDTamano")
    private Long sizeId;

}
