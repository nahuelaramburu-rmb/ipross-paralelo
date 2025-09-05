package com.capacidad.validationapi.prescription.galbop.model;

public enum GalbopPlanMapping {

    PLAN_BEHAVIOUR_1352(new int[]{2419, 2421, 2422, 2423, 2426, 2443}),
    PLAN_BEHAVIOUR_979(new int[]{2443}),
    PLAN_BEHAVIOUR_978(new int[]{2420}),
    PLAN_BEHAVIOUR_976(new int[]{2576, 2426}),
    PLAN_BEHAVIOUR_975(new int[]{2425, 2576}),
    PLAN_BEHAVIOUR_974(new int[]{2425}),
    PLAN_BEHAVIOUR_970(new int[]{2424, 2576}),
    PLAN_BEHAVIOUR_903(new int[]{2423, 2576}),
    PLAN_BEHAVIOUR_308(new int[]{2422, 2576}),
    PLAN_BEHAVIOUR_307(new int[]{2421}),
    PLAN_BEHAVIOUR_306(new int[]{2419}),
    PLAN_BEHAVIOUR_2506(new int[]{2576,2419}),
    DEFAULT(new int[]{0});

    private final int[] vademecumIds;

    GalbopPlanMapping(int[] vademecumIds) {
        this.vademecumIds = vademecumIds;
    }

    public int[] getVademecumIds() {
        return vademecumIds;
    }


}
