import React, { memo, useEffect } from 'react';
import { useDispatch, shallowEqual, useSelector } from 'react-redux';
import { View, StyleSheet, ActivityIndicator, Platform } from 'react-native';
import * as Colors from '../../constants/Colors';
import OpenProceduresList from '../../components/procedure/OpenProceduresList';
import get from 'lodash/get';
import ClosedProceduresList from '../../components/procedure/ClosedProceduresList';
import { getProcedures } from '../../actions/procedureAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../../components/header/HeaderWrapper';

const ProcedureScreen = () => {
    const dispatch = useDispatch();

    const { proceduresLoading, openedProcedures, closedProcedures } = useSelector(
        (state) => ({
            proceduresLoading: state.procedure.procedures.loading,
            openedProcedures: state.procedure.procedures.opened,
            closedProcedures: state.procedure.procedures.closed,
        }),
        shallowEqual
    );

    useEffect(() => {
        dispatch(getProcedures());
    }, [dispatch]);

    let view = null;

    if (proceduresLoading) {
        view = <ActivityIndicator size='large' color={Colors.primaryText} testID='procedures-loading' />;
    } else {
        view = (
            <React.Fragment>
                <OpenProceduresList
                    style={styles.openProceduresList}
                    procedures={get(openedProcedures, '_embedded.procedures', [])}
                />
                <ClosedProceduresList
                    style={styles.closedProceduresList}
                    procedures={get(closedProcedures, '_embedded.procedures', [])}
                />
            </React.Fragment>
        );
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <View style={styles.container}>{view}</View>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: Platform.OS === 'android' ? NAV_BAR_HEIGHT_ANDROID : NAV_BAR_HEIGHT_IOS,
    },
    openProceduresList: {
        flex: 1,
    },
    closedProceduresList: {
        flex: 2,
    },
});

export default memo(ProcedureScreen);
