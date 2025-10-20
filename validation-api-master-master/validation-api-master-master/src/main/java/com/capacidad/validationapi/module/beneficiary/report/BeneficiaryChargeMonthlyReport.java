package com.capacidad.validationapi.module.beneficiary.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryChargeMonthlyReport {

    private int month;
    private int year;
    private BigDecimal chargeTotal;

    public BeneficiaryChargeMonthlyReport(BigDecimal chargeTotal, int month, int year) {
        this.chargeTotal = chargeTotal;
        this.month = month;
        this.year = year;
    }

}
