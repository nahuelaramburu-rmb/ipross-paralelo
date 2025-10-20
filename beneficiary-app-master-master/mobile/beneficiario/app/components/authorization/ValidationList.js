import React, { useCallback } from 'react';
import { StyleSheet, Platform } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import ValidationItem from '../../components/authorization/ValidationItem';
import { verticalScale } from '../../lib/size-normalizer';
import { useNavigation } from '@react-navigation/native';
import { NAV_BAR_HEIGHT_IOS, NAV_BAR_HEIGHT_ANDROID } from '../header/HeaderWrapper';
import { getValidations, selectValidation } from '../../actions/validationAction';
import List from '../list';
import PropTypes from 'prop-types';

const TAB_BAR_HEIGHT = verticalScale(50);

const AuthorizationList = ({ filters, onScroll }) => {
    const dispatch = useDispatch();
    const navigation = useNavigation();

    const { validations, validationsLoading, validationsLoadingMore, validationLinks } = useSelector(
        (state) => ({
            validations: state.validation.authorizationList.validations._embedded?.authorizations ?? [],
            validationsLoading: state.validation.authorizationList.loading,
            validationsLoadingMore: state.validation.authorizationList.loadingMore,
            validationLinks: state.validation.authorizationList.validations._links ?? {},
        }),
        shallowEqual
    );

    const searchAuthorizations = useCallback(
        (isRefresh = false, filters = null, link = null) => {
            return dispatch(getValidations(isRefresh, filters, link));
        },
        [dispatch]
    );

    const itemPressed = useCallback(
        (item) => {
            dispatch(selectValidation(item.id));
            navigation.navigate('ValidationStatus');
        },
        [dispatch, navigation]
    );

    return (
        <List
            filters={filters}
            onScroll={onScroll}
            loading={validationsLoading}
            loadingMore={validationsLoadingMore}
            renderItem={({ item }) => <ValidationItem item={item} itemPressed={itemPressed} />}
            data={validations}
            getData={searchAuthorizations}
            links={validationLinks}
            contentInset={{ top: NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT }}
            contentOffset={{ x: 0, y: -(NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT) }}
            progressViewOffset={NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT} // only works on android
            contentContainerStyle={styles.flatlistWrapper}
            loadingContainerStyle={styles.loaderContainer}
        />
    );
};

AuthorizationList.propTypes = {
    filters: PropTypes.array,
    onScroll: PropTypes.object,
};

const styles = StyleSheet.create({
    flatlistWrapper: {
        flexGrow: 1,
        paddingTop: Platform.OS === 'ios' ? 0 : NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT,
    },
    loaderContainer: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        paddingTop:
            Platform.OS === 'ios'
                ? NAV_BAR_HEIGHT_IOS + TAB_BAR_HEIGHT
                : NAV_BAR_HEIGHT_ANDROID + TAB_BAR_HEIGHT,
    },
});

export default AuthorizationList;
