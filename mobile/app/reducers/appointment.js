import {
    APPOINTMENT_LOGGED_IN,
    APPOINTMENT_LOGGING_IN,
    FETCHING_APPOINTMENTS,
    FETCH_APPOINTMENTS_SUCCESS,
    FETCHING_APPLICANT,
    FETCH_APPLICANT_SUCCESS,
    FETCHING_DELEGATIONS,
    FETCH_DELEGATIONS_SUCCESS,
    FETCHING_SECTORS,
    FETCH_SECTORS_SUCCESS,
    FETCHING_APPOINTMENTS_ENABLED,
    FETCH_APPOINTMENTS_ENABLED_SUCCESS,
    APPOINTMENT_CREATE,
    CREATE_APPOINTMENT_SUCCESS,
    FETCHING_APPOINTMENT_BY_ID,
    FETCH_APPOINTMENT_BY_ID_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';

const initialState = {
    status: 'initializing',
    token: null,
    login: {
        loading: false,
    },
    appointments: {
        items: {},
        loading: false,
    },
    appointments_created: {
        items: {},
        creating_appointment: false,
    },
    applicant: {
        id: null,
        loading: false,
    },
    delegations: {
        items: {},
        loading: false,
    },
    sectors: {
        items: {},
        loading: false,
    },
    appointments_enabled: {
        items: {},
        loading: false,
    },
    selectedAppointment: {
        item: {},
        loading: false,
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case APPOINTMENT_LOGGED_IN:
            if (action.userapilogin !== null) {
                return {
                    ...state,
                    token: action.userapilogin.token,
                    status: 'logged_in',
                    login: {
                        ...state.login,
                        loading: false,
                    },
                };
            } else {
                return {
                    ...state,
                    status: 'logged_out',
                };
            }

        case APPOINTMENT_LOGGING_IN:
            return {
                ...state,
                login: {
                    ...state.login,
                    loading: true,
                },
            };

        case FETCHING_APPLICANT:
            return {
                ...state,
                applicant: {
                    ...state.applicant,
                    loading: true,
                },
            };

        case FETCH_APPLICANT_SUCCESS:
            return {
                ...state,
                applicant: {
                    ...state.applicant,
                    loading: false,
                    id: action.applicant.data.id,
                },
            };

        case FETCHING_APPOINTMENTS:
            return {
                ...state,
                appointments: {
                    ...state.appointments,
                    loading: true,
                },
            };

        case FETCH_APPOINTMENTS_SUCCESS:
            return {
                ...state,
                appointments: {
                    ...state.appointments,
                    loading: false,
                    items: action.appointments,
                },
            };
        case FETCHING_APPOINTMENT_BY_ID:
            return {
                ...state,
                selectedAppointment: {
                    ...state.selectedAppointment,
                    loading: true,
                },
            };
        case FETCH_APPOINTMENT_BY_ID_SUCCESS:
            return {
                ...state,
                selectedAppointment: {
                    ...state.selectedAppointment,
                    loading: false,
                    item: action.appointments.data,
                },
            };

        case FETCHING_DELEGATIONS:
            return {
                ...state,
                delegations: {
                    ...state.delegations,
                    loading: true,
                },
                sectors: {
                    ...state.sectors,
                    items: {},
                },
                appointments_enabled: {
                    ...state.appointments_enabled,
                    items: {},
                },
            };

        case FETCH_DELEGATIONS_SUCCESS:
            return {
                ...state,
                delegations: {
                    ...state.delegations,
                    loading: false,
                    items: action.delegations,
                },
            };
        case FETCHING_SECTORS:
            return {
                ...state,
                sectors: {
                    ...state.sectors,
                    items: {},
                    loading: true,
                },
                appointments_enabled: {
                    ...state.appointments_enabled,
                    items: {},
                },
            };

        case FETCH_SECTORS_SUCCESS:
            return {
                ...state,
                sectors: {
                    ...state.sectors,
                    loading: false,
                    items: action.sectors,
                },
            };

        case FETCHING_APPOINTMENTS_ENABLED:
            return {
                ...state,
                appointments_enabled: {
                    ...state.appointments_enabled,
                    items: {},
                    loading: true,
                },
            };

        case FETCH_APPOINTMENTS_ENABLED_SUCCESS:
            return {
                ...state,
                appointments_enabled: {
                    ...state.appointments_enabled,
                    loading: false,
                    items: action.turnos,
                },
            };
        case APPOINTMENT_CREATE:
            return {
                ...state,
                appointment_created: {
                    ...state.appointment_created,
                    creating_appointment: true,
                },
            };
        case CREATE_APPOINTMENT_SUCCESS:
            return {
                ...state,
                appointments_created: {
                    ...state.appointment_created,
                    creating_appointment: false,
                    items: action.appointment_created,
                },
            };
        case ERROR:
            return {
                ...state,
                appointments: {
                    ...state.appointments,
                    loading: false,
                },
                login: {
                    ...state.login,
                    loading: false,
                },
                appointments_enabled: {
                    ...state.appointments_enabled,
                    loading: false,
                },
            };

        default:
            return state;
    }
};
