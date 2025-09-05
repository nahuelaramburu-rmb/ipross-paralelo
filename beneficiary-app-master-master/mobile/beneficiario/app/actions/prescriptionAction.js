import {
    FETCHING_PRESCRIPTIONS,
    FETCH_PRESCRIPTIONS_SUCCESS,
    FETCHING_MORE_PRESCRIPTIONS,
    FETCH_MORE_PRESCRIPTIONS_SUCCESS,
    SELECT_PRESCRIPTION,
    FETCHING_PRESCRIPTION,
    FETCH_PRESCRIPTION_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';
import get from 'lodash/get';
import isEmpty from 'lodash/isEmpty';
import { apiUrls } from '../configs/api';

export const getPrescriptions = (isRefresh = false, queryFilter = null, link = null) => (
    dispatch,
    getState
) => _getPrescriptions(isRefresh, queryFilter, link, dispatch, getState);
const _getPrescriptions = async (isRefresh, queryFilter, link, dispatch, getState) => {
    const token = getState().profile.token;

    if (!isRefresh && !link) dispatch({ type: FETCHING_PRESCRIPTIONS });

    if (!isRefresh && link) dispatch({ type: FETCHING_MORE_PRESCRIPTIONS });

    const selectedUser = getState().profile.relatives.selectedUser;

    let url = link ? link : selectedUser._links.prescriptions.href;
    if (queryFilter && !link) url += `&${queryFilter}`;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        if (link) {
            return dispatch({
                type: FETCH_MORE_PRESCRIPTIONS_SUCCESS,
                prescriptions: json,
            });
        }
        
        return dispatch({
            type: FETCH_PRESCRIPTIONS_SUCCESS,
            prescriptions: json,
        });


    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const selectPrescription = (prescriptionId) => (dispatch, getState) =>
    _selectPrescription(prescriptionId, dispatch, getState);
const _selectPrescription = async (prescriptionId, dispatch, getState) => {
    const selectedPrescription =
        get(getState(), 'prescription.prescriptionList.items._embedded.prescriptions', []).find(
            (item) => item.id === parseInt(prescriptionId)
        ) || {};

    if (!isEmpty(selectedPrescription)) {
        return dispatch({
            type: SELECT_PRESCRIPTION,
            prescription: selectedPrescription,
        });
    }

    const token = getState().profile.token;
    dispatch({ type: FETCHING_PRESCRIPTION });

    try {
        const response = await fetch(apiUrls['api'] + `prescriptions/${prescriptionId}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            throw json;
        }

        dispatch({ type: FETCH_PRESCRIPTION_SUCCESS, prescription: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};
