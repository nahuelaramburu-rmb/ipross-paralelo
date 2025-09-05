package com.capacidad.validationapi.module.prescription.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.medicine.projection.MedicineProjection;

public interface PrescriptionItemProjection extends BaseProjection<Long> {

    MedicineProjection getMedicine();

    Integer getQuantity();

    String getDailyDosage();

    ICD10DiseaseProjection getDisease();

    Integer getTreatmentDays();

    interface Integration {

        MedicineProjection.Integration getMedicine();

        Integer getQuantity();

        String getDailyDosage();

    }

}
