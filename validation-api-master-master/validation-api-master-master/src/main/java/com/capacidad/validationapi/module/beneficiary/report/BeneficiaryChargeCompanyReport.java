package com.capacidad.validationapi.module.beneficiary.report;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryChargeCompanyReport {

    private int month;
    private int year;
    private String company;
    private BigDecimal chargeTotal;
    private String paymentMethod;

    public BeneficiaryChargeCompanyReport(int month, int year, String company, BigDecimal chargeTotal, String paymentMethod) {
        this.month = month;
        this.year = year;
        this.company = company;
        this.chargeTotal = chargeTotal;
        this.paymentMethod = paymentMethod;
    }

}
