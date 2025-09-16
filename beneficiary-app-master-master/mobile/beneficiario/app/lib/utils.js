import moment from 'moment';
import * as Colors from '../constants/Colors';

const makeQueryFilters = (filters) => {
    let query = filters.reduce((prevalue, actualValue, index) => {
        if (actualValue.value) {
            if (actualValue.type === 'date')
                return prevalue
                    .concat(
                        actualValue.remoteField,
                        '>',
                        moment(actualValue.value.startDate).format('YYYY-MM-DDTHH:mm:ss'),
                        ','
                    )
                    .concat(
                        actualValue.remoteField,
                        '<',
                        moment(actualValue.value.endDate).format('YYYY-MM-DDTHH:mm:ss'),
                        ','
                    );
        }
    }, 'search=');
    return query.slice(0, query.length - 1);
};

const getStatusColor = (status) => {
    const approvedStatusesColor = [
        posibleStatuses.APPROVED,
        posibleStatuses.ENABLED,
        posibleStatuses.CONFIRMADO,
        posibleStatuses.WITHOUT_COVERAGE,
        posibleStatuses.ACTIVE,
        posibleStatuses.APPROVED_FEM,
        posibleStatuses.ACTIVE_FEM,
        posibleStatuses.ASISTIO,
    ];
    const rejectedStatusesColor = [
        posibleStatuses.REJECTED,
        posibleStatuses.CANCELADO,
        posibleStatuses.DISABLED,
        posibleStatuses.CANCELLED,
        posibleStatuses.EXPIRED,
        posibleStatuses.EXPIRED_FEM,
        posibleStatuses.CANCELLED_FEM,
        posibleStatuses.ANNULLED,
        posibleStatuses.ANNULLED_FEM,
        posibleStatuses.NO_ASISTIO,
    ];
    const intermediateStatusesColor = [
        posibleStatuses.CONSUMED_FEM,
        posibleStatuses.PARTIALLY_APPROVED,
        posibleStatuses.CONSUMED,
    ];
    const pendingStatusColor = [
        posibleStatuses.PENDING,
        posibleStatuses.PENDING_APPROVAL,
        posibleStatuses.REVIEWING,
        posibleStatuses.UNKNOWN,
        posibleStatuses.ENESPERA,
    ];

    if (approvedStatusesColor.indexOf(status) > -1) return Colors.statusApproved;
    if (rejectedStatusesColor.indexOf(status) > -1) return Colors.statusRejected;
    if (intermediateStatusesColor.indexOf(status) > -1) return Colors.statusIntermediate;
    if (pendingStatusColor.indexOf(status) > -1) return Colors.statusPending;

    return '#E1E1E1';
};

const uuidv4 = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        var r = (Math.random() * 16) | 0,
            v = c == 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
};

const posibleStatuses = {
    ENABLED: 'HABILITADO',
    DISABLED: 'DESHABILITADO',
    PENDING_APPROVAL: 'PENDIENTE DE APROBACION', // Auths
    APPROVED: 'APROBADO', // Procedures - Auths
    CONFIRMADO: 'CONFIRMADO', //Turnos
    CANCELADO: 'CANCELADO', //Turnos
    ASISTIO: 'ASISTIÓ',
    NO_ASISTIO: 'NO ASISTIÓ',
    ENESPERA: 'EN ESPERA',
    PARTIALLY_APPROVED: 'PARCIALMENTE APROBADO', // Auths
    REJECTED: 'RECHAZADO', // Procedures - Auths
    OPENED_SETTLEMENT: 'LIQUIDACION ABIERTA',
    WITH_COVERAGE: 'CON COBERTURA',
    WITHOUT_COVERAGE: 'SIN COBERTURA',
    CANCELLED: 'CANCELADO', // Auths - Batchs
    CLOSED_SETTLEMENT: 'LIQUIDACION CERRADA',
    AUTHORIZED: 'AUTORIZADO',
    PAYED: 'RENDIDO',
    NOT_PAYED: 'NO RENDIDO',
    REVIEWING: 'EN REVISION', // Procedures
    PENDING: 'PENDIENTE', // Batchs - Prescriptions
    EXPIRED: 'VENCIDO', // Procedures - Batchs
    ACTIVE: 'ACTIVO', // Batchs
    ACTIVE_FEM: 'ACTIVA', // Pre-Auths
    APPROVED_FEM: 'APROBADA', // Prescriptions
    REJECTED_FEM: 'RECHAZADA', // Prescriptions
    CANCELLED_FEM: 'CANCELADA', // Prescriptions
    CONSUMED_FEM: 'UTILIZADA', // Prescriptions
    EXPIRED_FEM: 'VENCIDA', // Pre-Auths - Prescriptions
    UNKNOWN: 'DESCONOCIDO',
    ANNULLED: 'ANULADO',
    ANNULLED_FEM: 'ANULADA', // Pre-Auths
    CONSUMED: 'CONSUMIDA', // Pre-Auths
};

const toHex = (string) => {
    var result = '';
    for (var i = 0; i < string.length; i++) {
        result += string.charCodeAt(i).toString(16);
    }
    return result;
};

export { makeQueryFilters, getStatusColor, uuidv4, posibleStatuses, toHex };
