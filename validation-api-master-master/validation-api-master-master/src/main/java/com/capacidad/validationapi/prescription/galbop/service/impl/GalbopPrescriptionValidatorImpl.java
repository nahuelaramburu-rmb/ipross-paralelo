package com.capacidad.validationapi.prescription.galbop.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPlan;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPlanEmbeddedId;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;
import com.capacidad.validationapi.prescription.galbop.model.QuantityRule;
import com.capacidad.validationapi.prescription.galbop.repository.GalbopPlanRepository;
import com.capacidad.validationapi.prescription.galbop.service.GalbopPrescriptionValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.prescription.galbop.service.impl.GalbopPrescriptionServiceImpl.GALBOP_DEFAULT_FUNDER_ID;

@Profile({"!test"})
@Component
public class GalbopPrescriptionValidatorImpl implements GalbopPrescriptionValidator {

    private static final int DEFAULT_SIZE_ID = 3;
    private final GalbopPlanRepository galbopPlanRepository;

    @Autowired
    public GalbopPrescriptionValidatorImpl(GalbopPlanRepository galbopPlanRepository) {
        this.galbopPlanRepository = galbopPlanRepository;
    }

    @Override
    public void validate(GalbopPrescription galbopPrescription, Prescription prescription) throws ObjectNotFoundException, ObjectNotValidException {
        long planId = galbopPrescription.getId().getPlanId();
        GalbopPlan galbopPlan = findGalbopPlan(planId);
        if (StringUtils.isNotBlank(galbopPlan.getQuantityRules()))
            validateQuantityRules(galbopPlan, prescription);
    }

    private GalbopPlan findGalbopPlan(long planId) throws ObjectNotFoundException {
        GalbopPlanEmbeddedId galbopPlanEmbeddedId = new GalbopPlanEmbeddedId();
        galbopPlanEmbeddedId.setPlanId((int) planId);
        galbopPlanEmbeddedId.setFunderId(GALBOP_DEFAULT_FUNDER_ID);
        return galbopPlanRepository.findById(galbopPlanEmbeddedId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Invalid Galbop Plan Id: %d", planId)));
    }

    private void validateQuantityRules(GalbopPlan galbopPlan, Prescription prescription) throws ObjectNotValidException {
        Map<Long, QuantityRule> sizeRules = parseSizeRulesAndCollectBySizeId(galbopPlan);
        Map<Long, Integer> totalQuantitiesBySizeId = new HashMap<>();
        Map<Long, Medicine> medicineMap = new HashMap<>();
        for (PrescriptionItem prescriptionItem : prescription.getPrescriptionItems()) {
            Medicine medicine = prescriptionItem.getMedicine();
            long appliedSizeId = verifyMaxInLineRuleAndReturnSizeId(sizeRules, medicine.getProduct(), medicine.getSizeId(), prescriptionItem.getQuantity());
            int currentSizeQuantity = totalQuantitiesBySizeId.computeIfAbsent(appliedSizeId, i -> 0);
            totalQuantitiesBySizeId.put(appliedSizeId, currentSizeQuantity + prescriptionItem.getQuantity());
            medicineMap.put(appliedSizeId, medicine);
        }
        verifyMaxInPrescriptionRule(sizeRules, totalQuantitiesBySizeId, medicineMap);
    }

    private Map<Long, QuantityRule> parseSizeRulesAndCollectBySizeId(GalbopPlan galbopPlan) {
        String sizeRuleString = galbopPlan.getQuantityRules();
        String[] sizeRuleStringArray = StringUtils.split(sizeRuleString, ";");
        Pattern pattern = Pattern.compile("(\\p{Digit}+),(\\p{Digit}+),(\\p{Digit}+)");
        return Arrays.stream(sizeRuleStringArray)
                .map(s -> {
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find())
                        return QuantityRule.builder()
                                .sizeId(Integer.parseInt(matcher.group(1)))
                                .maxInLine(Integer.parseInt(matcher.group(2)))
                                .maxInPrescription(Integer.parseInt(matcher.group(3)))
                                .build();
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(r -> Long.valueOf(r.getSizeId()), Function.identity()));
    }

    private long verifyMaxInLineRuleAndReturnSizeId(Map<Long, QuantityRule> sizeRules, String productName, long sizeId, int quantity) throws ObjectNotValidException {
        return sizeRules.containsKey(sizeId) ? applyMaxInLineRuleAndReturnSizeId(sizeRules, productName, sizeId, quantity) :
                applyMaxInLineRuleAndReturnSizeId(sizeRules, productName, DEFAULT_SIZE_ID, quantity);
    }

    private long applyMaxInLineRuleAndReturnSizeId(Map<Long, QuantityRule> sizeRules, String productName, long sizeId, int quantity) throws ObjectNotValidException {
        QuantityRule quantityRule = sizeRules.get(sizeId);
        if (quantity > quantityRule.getMaxInLine())
            throw new ObjectNotValidException("prescription.invalidMaxInLine", productName, String.valueOf(quantity), String.valueOf(quantityRule.getMaxInLine()));
        return sizeId;
    }

    private void verifyMaxInPrescriptionRule(Map<Long, QuantityRule> sizeRules, Map<Long, Integer> totalQuantitiesBySizeId, Map<Long, Medicine> medicineMap) throws ObjectNotValidException {
        for (Map.Entry<Long, Integer> entry : totalQuantitiesBySizeId.entrySet()) {
            QuantityRule quantityRule = sizeRules.get(entry.getKey());
            if (entry.getValue() > quantityRule.getMaxInPrescription()) {
                String productName = medicineMap.get(entry.getKey()).getProduct();
                throw new ObjectNotValidException("prescription.invalidMaxInPrescription", productName, String.valueOf(entry.getValue()), String.valueOf(quantityRule.getMaxInPrescription()));
            }
        }
    }

}
