import {
    FETCHING_BATCH,
    FETCH_BATCH_SUCCESS,
    FETCHING_BATCHES,
    FETCH_BATCHES_SUCCESS,
    FETCHING_MORE_BATCHES,
    FETCH_MORE_BATCHES_SUCCESS,
    FETCHING_BATCH_ITEMS,
    FETCH_BATCH_ITEMS_SUCCESS,
    FETCHING_MORE_BATCH_ITEMS,
    FETCH_MORE_BATCH_ITEMS_SUCCESS,
    ERROR,
} from '../constants/ActionTypes';

export const getBatches =
    (isRefresh = false, queryFilter = null, link = null) =>
    (dispatch, getState) =>
        _getBatches(isRefresh, queryFilter, link, dispatch, getState);
const _getBatches = async (isRefresh, queryFilter, link, dispatch, getState) => {
    const token = getState().profile.token;

    if (!isRefresh && !link) dispatch({ type: FETCHING_BATCHES });

    if (!isRefresh && link) dispatch({ type: FETCHING_MORE_BATCHES });

    const selectedUser = getState().profile.relatives.selectedUser;

    const url = link ? link : selectedUser._links.batches.href;

    console.log(token);

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
                type: FETCH_MORE_BATCHES_SUCCESS,
                batches: json,
            });
        }

        return dispatch({
            type: FETCH_BATCHES_SUCCESS,
            batch: json,
            isRefresh: isRefresh,
        });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: err });
        throw err;
    }
};

export const getBatch = (link) => (dispatch, getState) => _getBatch(link, dispatch, getState);
const _getBatch = async (link, dispatch, getState) => {
    dispatch({ type: FETCHING_BATCH });

    const token = getState().profile.token;

    try {
        const response = await fetch(link, {
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

        dispatch({ type: FETCH_BATCH_SUCCESS, batch: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};

export const getBatchItems = (isRefresh, filters, link) => (dispatch, getState) =>
    _getBatchItems(isRefresh, filters, link, dispatch, getState);

const _getBatchItems = async (isRefresh, filters, link, dispatch, getState) => {
    if (isRefresh) dispatch({ type: FETCHING_BATCH_ITEMS });

    if (!isRefresh) dispatch({ type: FETCHING_MORE_BATCH_ITEMS });

    const token = getState().profile.token;

    try {
        const response = await fetch(link, {
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

        if (!isRefresh) {
            return dispatch({
                type: FETCH_MORE_BATCH_ITEMS_SUCCESS,
                batchItems: json,
            });
        }

        return dispatch({ type: FETCH_BATCH_ITEMS_SUCCESS, batchItems: json });
    } catch (err) {
        console.log(err);
        dispatch({ type: ERROR, error: { error: err } });
    }
};
