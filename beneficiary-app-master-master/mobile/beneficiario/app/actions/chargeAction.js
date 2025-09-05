import {
    FETCHING_CHARGES,
    FETCH_CHARGES_SUCCESS,
    CLEAR_CHARGES_STATUS,
    FETCH_MORE_CHARGES_SUCCESS,
    FETCHING_MORE_CHARGES,
    ERROR,
} from '../constants/ActionTypes';

export const getUserCharges = (isRefresh) => (dispatch, getState) =>
    _getUserCharges(isRefresh, dispatch, getState);
const _getUserCharges = async (isRefresh, dispatch, getState) => {
    if (!isRefresh) dispatch({ type: FETCHING_CHARGES });

    const token = getState().profile.token;
    const selectedUser = getState().profile.relatives.selectedUser;

    try {
        const response = await fetch(selectedUser._links.charges.href, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            dispatch({ type: ERROR, error: json });
            return;
        }

        dispatch({ type: FETCH_CHARGES_SUCCESS, charges: json, isRefresh: isRefresh });

    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const clearStatus = () => (dispatch) => _clearStatus(dispatch);
const _clearStatus = (dispatch) => {
    dispatch({ type: CLEAR_CHARGES_STATUS });
};

export const searchNextPage = () => (dispatch, getState) => _searchNextPage(dispatch, getState);
const _searchNextPage = async (dispatch, getState) => {
    const nextPage = getState().charge.charges.items._links?.next ?? undefined;
    if (typeof nextPage === 'undefined') return;
    dispatch({ type: FETCHING_MORE_CHARGES });
    const token = getState().profile.token;

    try {
        const response = await fetch(nextPage.href, {
            method: 'GET',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: token.token_type + ' ' + token.access,
            },
        });

        const json = await response.json();

        if (!response.ok) {
            dispatch({ type: ERROR, error: json });
            return;
        }

        dispatch({ type: FETCH_MORE_CHARGES_SUCCESS, charges: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};
