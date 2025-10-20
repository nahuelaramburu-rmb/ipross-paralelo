package com.capacidad.validationapi.prescription.integration;

public interface MedicinesIntegration {

    ProductWrapperDTO getProducts(String name, String beneficiaryCode);

}
