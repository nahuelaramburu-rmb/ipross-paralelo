package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopBeneficiary;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopBeneficiaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalbopBeneficiaryRepository extends JpaRepository<GalbopBeneficiary, Long> {


    @Query(nativeQuery = true, value = "SELECT a.IdPlan as planId, pl.prioridad as priority, pl.IDComportamiento as planBehaviour " +
            "FROM Afiliados a INNER JOIN planes pl ON(a.idpadron=pl.IDObSoc and a.IdPlan=pl.IdPlan and pl.tipo='A') " +
            "WHERE a.IDPadron=2050 AND a.IDAfiliado LIKE ?1 AND a.IDBeneficiario='0' AND (a.FechaBaja is null OR a.FechaBaja='1899-12-30' OR a.FechaBaja>CurDate()) " +
            "ORDER BY pl.prioridad DESC")
    List<GalbopBeneficiaryProjection> findGalbopBeneficiaryPlans(String beneficiaryCode);

}
