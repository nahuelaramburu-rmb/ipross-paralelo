import validation from '../validation';
import * as ActionTypes from '../../constants/ActionTypes';

const initialState = {
    loading: false,
    validations: {},
    status: null,
    loadingMore: false,
    selected_validation: {
        item: {},
        associated_files: [],
        loading_files: false,
        loading: false,
    },
};

describe('validation reducer tests', () => {
    it('should return the initial state', () => {
        expect(validation(undefined, {})).toEqual(initialState);
    });

    it('should handle FETCHING_VALIDATIONS', () => {
        const fetchingValidationsAction = {
            type: ActionTypes.FETCHING_VALIDATIONS,
        };

        const expectedState = {
            ...initialState,
            loading: true,
            status: null,
        };

        expect(validation(undefined, fetchingValidationsAction)).toEqual(expectedState);
    });

    it('should handle FETCH_VALIDATIONS_SUCCESS with refresh false', () => {
        const fetchValidationsSuccessAction = {
            type: ActionTypes.FETCH_VALIDATIONS_SUCCESS,
            validations: {
                _links: { link1: { href: 'test.com' } },
                _embedded: [
                    { id: 1, date: '10/10/2010' },
                    { id: 2, date: '14/10/2010' },
                ],
            },
            isRefresh: false,
        };

        const expectedState = {
            ...initialState,
            loading: false,
            validations: {
                _links: { link1: { href: 'test.com' } },
                _embedded: [
                    { id: 1, date: '10/10/2010' },
                    { id: 2, date: '14/10/2010' },
                ],
            },
            status: 'validations_fetched',
        };

        expect(validation(undefined, fetchValidationsSuccessAction)).toEqual(expectedState);
    });

    it('should handle FETCH_VALIDATIONS_SUCCESS with refresh true', () => {
        const fetchValidationsSuccessAction = {
            type: ActionTypes.FETCH_VALIDATIONS_SUCCESS,
            validations: {
                _links: { link1: { href: 'test.com' } },
                _embedded: [
                    { id: 1, date: '10/10/2010' },
                    { id: 2, date: '14/10/2010' },
                ],
            },
            isRefresh: true,
        };

        const expectedState = {
            ...initialState,
            loading: false,
            validations: {
                _links: { link1: { href: 'test.com' } },
                _embedded: [
                    { id: 1, date: '10/10/2010' },
                    { id: 2, date: '14/10/2010' },
                ],
            },
            status: 'validations_updated',
        };

        expect(validation(undefined, fetchValidationsSuccessAction)).toEqual(expectedState);
    });

    it('should handle CLEAR_VALIDATION_STATUS', () => {
        const clearValidationStatusAction = {
            type: ActionTypes.CLEAR_VALIDATION_STATUS,
        };

        const expectedState = {
            ...initialState,
            status: null,
        };

        expect(validation(undefined, clearValidationStatusAction)).toEqual(expectedState);
    });

    it('should handle FETCHING_MORE_VALIDATIONS', () => {
        const fetchingMoreValidationsAction = {
            type: ActionTypes.FETCHING_MORE_VALIDATIONS,
        };

        const expectedState = {
            ...initialState,
            loadingMore: true,
            status: null,
        };

        expect(validation(undefined, fetchingMoreValidationsAction)).toEqual(expectedState);
    });

    it('should handle FETCH_MORE_VALIDATION_SUCCESS', () => {
        const fetchMoreValidationsSuccessAction = {
            type: ActionTypes.FETCH_MORE_VALIDATION_SUCCESS,
            validations: {
                _links: { next: { href: 'capacidad.com?page=2&size=50' } },
                _embedded: { authorizations: [{ id: 3, date: '20/10/10' }] },
            },
        };

        const preloadInitialState = {
            ...initialState,
            validations: {
                _links: {
                    next: 'capacidad.com?page=1&size=50',
                },
                _embedded: {
                    authorizations: [
                        { id: 1, date: '10/10/2010' },
                        { id: 2, date: '14/10/2010' },
                    ],
                },
            },
        };

        const expectedState = {
            ...initialState,
            validations: {
                _links: {
                    next: { href: 'capacidad.com?page=2&size=50' },
                },
                _embedded: {
                    authorizations: [
                        { id: 1, date: '10/10/2010' },
                        { id: 2, date: '14/10/2010' },
                        { id: 3, date: '20/10/10' },
                    ],
                },
            },
            loadingMore: false,
            status: null,
        };

        expect(validation(preloadInitialState, fetchMoreValidationsSuccessAction)).toEqual(expectedState);
    });

    it('should handle ERROR', () => {
        const errorAction = {
            type: ActionTypes.ERROR,
        };

        const expectedState = {
            ...initialState,
            loading: false,
            loadingMore: false,
        };

        expect(validation(undefined, errorAction)).toEqual(expectedState);
    });
});
