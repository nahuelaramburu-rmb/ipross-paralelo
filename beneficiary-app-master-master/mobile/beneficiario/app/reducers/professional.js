/* @flow */

import {
    FETCHING_TYPE_SPECIALTY,
    FETCH_TYPE_SPECIALTY_SUCCESS,
    FETCHING_ESPECIALTY,
    FETCH_ESPECIALTY_SUCCESS,
    FETCHING_TOWN,
    FETCH_TOWN_SUCCESS,
    FETCHING_PROFESSIONAL,
    FETCH_PROFESSIONAL_SUCCESS,
    FETCH_PROFESSIONALS_SUCCESS,
    FETCHING_PROFESSIONALS,
    FETCHING_MORE_PROFESSIONALS,
    FETCH_MORE_PROFESSIONALS_SUCCESS,
    FETCHING_MEDICAL_CENTER,
    FETCH_MEDICAL_CENTER_SUCCESS,
    FETCHING_MEDICAL_COORDINATES,
    FETCH_MEDICAL_COORDINATES_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';

const initialState = {
    items: {},
    loading: false,
    loadingMore: false,

    //TYPE SPECIALTY = TIPO ESPECIALIDAD
    typesSpecialty: {
        items: [],
        loading: false,
    },
    //SPECIALTY = ESPECIALIDAD
    specialties: {
        items: [],
        loading: false,
    },
    //TOWN = LOCALIDAD
    towns: {
        items: [],
        loading: false,
    },

    //PROFESIONALES PRACTITIONER
    selectedPractitioner: {
        item: {},
        loading: false,
    },

    medicalCenters: {
        items: {},
        loading: false,
    },
    medicalCoordinates: {
        items: {},
        loading: false,
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case FETCHING_TYPE_SPECIALTY:
            return {
                ...state,
                typesSpecialty: {
                    ...state.typesSpecialty,
                    loading: true,
                },
            };
        case FETCH_TYPE_SPECIALTY_SUCCESS:
            return {
                ...state,
                typesSpecialty: {
                    ...state.typesSpecialty,
                    loading: false,
                    items: action.typesSpecialty,
                },
            };
        case FETCHING_ESPECIALTY:
            return {
                ...state,
                specialties: {
                    ...state.specialties,
                    loading: true,
                },
            };
        case FETCH_ESPECIALTY_SUCCESS:
            return {
                ...state,
                specialties: {
                    ...state.specialties,
                    loading: false,
                    items: action.specialties,
                },
            };

        case FETCHING_TOWN:
            return {
                ...state,
                towns: {
                    ...state.towns,
                    loading: true,
                },
            };
        case FETCH_TOWN_SUCCESS:
            return {
                ...state,
                towns: {
                    ...state.towns,
                    loading: false,
                    items: action.towns,
                },
            };

        case FETCHING_PROFESSIONALS:
            return {
                ...state,
                loading: true,
            };
        case FETCH_PROFESSIONALS_SUCCESS:
            return {
                ...state,
                loading: false,
                items: action.practitioners,
            };

        case FETCHING_PROFESSIONAL:
            return {
                ...state,
                selectedPractitioner: {
                    ...state.selectedPractitioner,
                    loading: true,
                },
            };
        case FETCH_PROFESSIONAL_SUCCESS:
            return {
                ...state,
                selectedPractitioner: {
                    ...state.selectedPractitioner,
                    loading: false,
                    item: action.practitioner,
                },
            };

        case FETCHING_MORE_PROFESSIONALS:
            return {
                ...state,
                items: {
                    ...state.items,
                    loadingMore: true,
                },
            };

        case FETCH_MORE_PROFESSIONALS_SUCCESS:
            let _embedded = [...state.items._embedded.practitioners.concat(action.items._embedded.items)];
            let _links = action.items._links;
            let items = { ...state.items, _links, _embedded: { items: _embedded } };
            return {
                ...state,
                items: {
                    ...state.items,
                    items: items,
                    loadingMore: false,
                },
            };
        case FETCHING_MEDICAL_CENTER:
            return {
                ...state,
                medicalCenters: {
                    ...state.medicalCenters,
                    loading: true,
                },
            };
        case FETCH_MEDICAL_CENTER_SUCCESS:
            return {
                ...state,
                medicalCenters: {
                    ...state.medicalCenters,
                    loading: false,
                    items: action.medicalCenters,
                },
            };

        case FETCHING_MEDICAL_COORDINATES:
            return {
                ...state,
                medicalCoordinates: {
                    ...state.medicalCoordinates,
                    loading: true,
                },
            };
        case FETCH_MEDICAL_COORDINATES_SUCCESS:
            return {
                ...state,
                medicalCoordinates: {
                    ...state.medicalCoordinates,
                    loading: false,
                    items: action.medicalCoordinates,
                },
            };

        case ERROR:
            return {
                ...state,
                loading: true,

                typesSpecialty: {
                    ...state.typesSpecialty,
                    loading: false,
                },
                specialties: {
                    ...state.specialties,
                    loading: false,
                },
                towns: {
                    ...state.towns,
                    loading: false,
                },
                selectedPractitioner: {
                    ...state.selectedPractitioner,
                    loading: false,
                },
                medicalCenters: {
                    ...state.medicalCenters,
                    loading: false,
                },
            };
        default:
            return state;
    }
};

function handleMoreProfessionalsSuccess(state, action) {
    return {
        ...state,
        loadingMore: false,
        items: {
            ...state.items,
            _links: action.practitioners._links,
            _embedded: {
                ...state.items._embeddded,
                practitioners: [
                    ...state.items._embedded.practitioners,
                    ...action.practitioners._embedded.practitioners,
                ],
            },
        },
    };
}

function handleFetchProfessionalSuccess(state, action) {
    if (!state.items._embedded) return state;

    const alreadyLoadedPrescr = [...state.items._embedded.practitioners];
    const indx = alreadyLoadedPrescr.findIndex((it) => it.id === action.prescription.id);
    if (indx > -1) return state;
    alreadyLoadedPrescr.unshift(action.prescription);

    return {
        ...state,
        items: {
            ...state.items,
            _embedded: {
                ...state.items._embedded,
                practitioners: alreadyLoadedPrescr,
            },
        },
    };
}
