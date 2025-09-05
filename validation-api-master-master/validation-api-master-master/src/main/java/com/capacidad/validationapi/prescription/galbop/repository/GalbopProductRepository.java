package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopProduct;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GalbopProductRepository extends JpaRepository<GalbopProduct, Double> {

    @Query(nativeQuery = true, value = "SELECT distinct(p.IDProducto) as exchangeId, " +
            "p.Producto as recommendation, " +
            "trim(dr.Descripcion) as product, " +
            "concat_ws(' ', f.Descripcion, cast(p.Concentracion as char), trim(tc.Descripcion), 'x', cast(p.Unidades as char), trim(u.Descripcion)) as description, " +
            "p.Presentacion as presentation, " +
            "p.Concentracion as concentration, " +
            "p.Unidades as units, " +
            "p.IDTipoConc as concentrationTypeId, " +
            "p.IDForma as productTypeId, " +
            "p.IDTipoUnidad as unitsTypeId, " +
            "e.IDDroga as drugId, " +
            "p.IDTamano as sizeId, " +
            "p.Unidades * u.Valor as authorizedDosage " +
            "FROM productos p " +
            "JOIN enlacedrogas e USING(IDProducto) " +
            "JOIN drogas dr USING(IDDroga) " +
            "JOIN formas f USING(IDForma) " +
            "JOIN tiposunidades u ON(u.IDTipoUnidad=p.IDTipoUnidad) " +
            "JOIN tiposconcentracion tc USING(IDTipoConc) " +
            "JOIN vademecumregistros vr on(IDProducto=Codigo) " +
            "WHERE vr.IDVad in ?2 AND vr.FechaBaja IS NULL AND vr.Autoriza='S' AND p.Activo<>'N' " +
            "AND (trim(dr.Descripcion) LIKE ?1 " +
            "OR p.Producto LIKE ?1 " +
            "OR concat_ws(' ', trim(dr.Descripcion), p.Presentacion) LIKE ?1 " +
            "OR concat_ws(' ', trim(dr.Descripcion), p.Concentracion) LIKE ?1 " +
            "OR concat_ws(' ', trim(dr.Descripcion), f.Descripcion) LIKE ?1 " +
            "OR concat_ws(' ', trim(dr.Descripcion), u.Descripcion) LIKE ?1) " +
            "GROUP BY e.IDDroga, p.IDProducto")
    Set<ProductIntegrationProjection> findAllByNameAndVademecumId(String name, int[] vademecumId);

    @Query(nativeQuery = true, value = "SELECT distinct(p.IDProducto) as exchangeId, " +
            "p.Producto as recommendation, " +
            "trim(dr.Descripcion) as product, " +
            "concat_ws(' ', f.Descripcion, cast(p.Concentracion as char), trim(tc.Descripcion), 'x', cast(p.Unidades as char), trim(u.Descripcion)) as description, " +
            "p.Presentacion as presentation, " +
            "p.Concentracion as concentration, " +
            "p.Unidades as units, " +
            "p.IDTipoConc as concentrationTypeId, " +
            "p.IDForma as productTypeId, " +
            "p.IDTipoUnidad as unitsTypeId, " +
            "e.IDDroga as drugId, " +
            "p.IDTamano as sizeId, " +
            "p.Unidades * u.Valor as authorizedDosage " +
            "FROM productos p " +
            "JOIN enlacedrogas e USING(IDProducto) " +
            "JOIN drogas dr USING(IDDroga) " +
            "JOIN formas f USING(IDForma) " +
            "JOIN tiposunidades u ON(u.IDTipoUnidad=p.IDTipoUnidad) " +
            "JOIN tiposconcentracion tc USING(IDTipoConc) " +
            "WHERE p.IDProducto in ?1")
    Set<ProductIntegrationProjection> findAllByProductIdIn(Set<Long> productId);

}
