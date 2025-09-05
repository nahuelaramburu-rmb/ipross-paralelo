/* @flow */

import {
    FETCHING_BATCHES,
    FETCH_BATCHES_SUCCESS,
    FETCHING_BATCH,
    FETCH_BATCH_SUCCESS,
    ERROR,
    FETCHING_MORE_BATCHES,
    FETCH_MORE_BATCHES_SUCCESS,
    FETCHING_BATCH_ITEMS,
    FETCH_BATCH_ITEMS_SUCCESS,
    FETCHING_MORE_BATCH_ITEMS,
    FETCH_MORE_BATCH_ITEMS_SUCCESS,
} from '../constants/ActionTypes';

const initialState = {
    batches: {
        // BATCHS DEL USUARIO SELECCIONADO EN REDUCER DE PROFILE (RELATIVE‡S)
        items: {},
        loading: false,
        loadingMore: false,
    },
    selectedBatch: {
        loading: false,
        item: {},
        batchItems: {
            batchId: 0,
            loading: false,
            loadingMore: false,
            items: {},
        },
    },
};

export default (state = initialState, action) => {
    switch (action.type) {
        case FETCHING_BATCHES:
            return {
                ...state,
                batches: {
                    ...state.batches,
                    loading: true,
                },
            };
        case FETCH_BATCHES_SUCCESS:
            return {
                ...state,
                batches: {
                    ...state.batches,
                    loading: false,
                    items: action.batch,
                },
            };
        case FETCHING_MORE_BATCHES:
            return {
                ...state,
                batches: {
                    ...state.batches,
                    loadingMore: true,
                },
            };
        case FETCH_MORE_BATCHES_SUCCESS:
            let _embedded = [
                ...state.batches.items._embedded.batches.concat(action.batches._embedded.batches),
            ];
            let _links = action.batches._links;
            let batches = { ...state.batches.items, _links, _embedded: { batches: _embedded } };
            return {
                ...state,
                batches: {
                    ...state.batches,
                    items: batches,
                    loadingMore: false,
                },
            };
        case FETCHING_BATCH:
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    loading: true,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        batchId: 0,
                        items: {},
                    },
                },
            };
        case FETCH_BATCH_SUCCESS:
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    loading: false,
                    item: action.batch,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        batchId: action.batch.id,
                    },
                },
            };
        case FETCHING_BATCH_ITEMS: {
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        loading: true,
                    },
                },
            };
        }
        case FETCH_BATCH_ITEMS_SUCCESS: {
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        loading: false,
                        items: action.batchItems,
                    },
                },
            };
        }
        case FETCHING_MORE_BATCH_ITEMS: {
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        loadingMore: true,
                    },
                },
            };
        }
        case FETCH_MORE_BATCH_ITEMS_SUCCESS: {
            let _embedded = [
                ...state.selectedBatch.batchItems.items._embedded.batchItems.concat(
                    action.batchItems._embedded.batchItems
                ),
            ];
            let _links = action.batchItems._links;
            let batchItems = {
                ...state.selectedBatch.batchItems.items,
                _links,
                _embedded: { batchItems: _embedded },
            };
            return {
                ...state,
                selectedBatch: {
                    ...state.selectedBatch,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        loadingMore: false,
                        items: batchItems,
                    },
                },
            };
        }
        case ERROR:
            return {
                ...state,
                batches: {
                    ...state.batches,
                    loading: false,
                },
                selectedBatch: {
                    ...state.selectedBatch,
                    loading: false,
                    batchItems: {
                        ...state.selectedBatch.batchItems,
                        loading: false,
                        loadingMore: false,
                    },
                },
            };
        default:
            return state;
    }
};
