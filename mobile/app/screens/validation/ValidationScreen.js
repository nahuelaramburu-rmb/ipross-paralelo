import React, { useState, useEffect, useCallback } from 'react';
import { Dimensions, Animated, Platform, StyleSheet } from 'react-native';
import { TabView, TabBar } from 'react-native-tab-view';
import ValidationList from '../../components/authorization/ValidationList';
import * as Colors from '../../constants/Colors';
import { verticalScale } from '../../lib/size-normalizer';
import PrescriptionList from '../../components/prescription/PrescriptionList';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../../components/header/HeaderWrapper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAnimatableHeader } from '../../hooks/utils';
import PropTypes from 'prop-types';

const initialLayout = { width: Dimensions.get('screen').width, height: Dimensions.get('screen').height };

const TAB_BAR_HEIGHT = verticalScale(50);

const ValidationScreen = ({ navigation }) => {
    const [index, setIndex] = useState(0);
    const [filters, setFilters] = useState([
        {
            key: 1,
            title: 'Fecha de Atención',
            type: 'date',
            remoteField: 'createdAt',
            description: 'Filtrar las validaciones entre dos fechas',
            value: null,
        },
    ]);

    const [routes] = useState([
        { key: 'authorizations', title: 'Atenciones' },
        { key: 'prescriptions', title: 'Recetas' },
    ]);

    const { interpolatedHeight, onScroll } = useAnimatableHeader();

    const applyFilters = useCallback(
        (filters) => {
            const filtersApplied = filters.filter((fil) => fil.value !== null);
            navigation.setParams({ filtersApplied: filtersApplied.length });
            setFilters([...filters]);
        },
        [navigation]
    );

    const goToFilterModal = useCallback(() => {
        navigation.navigate('FilterModal', {
            filters: filters,
            onSaveFilters: (filters) => applyFilters(filters),
        });
    }, [navigation, filters, applyFilters]);

    const setHeaderParams = useCallback(() => {
        // PARAMS FOR FILTER MODAL
        navigation.setParams({
            openModal: goToFilterModal,
        });
    }, [goToFilterModal, navigation]);

    useEffect(() => {
        setHeaderParams();
    }, [setHeaderParams]);

    const renderScene = ({ route }) => {
        switch (route.key) {
            case 'authorizations':
                return <ValidationList onScroll={onScroll} filters={filters} />;
            case 'prescriptions':
                return <PrescriptionList onScroll={onScroll} filters={filters} />;
            default:
                return null;
        }
    };

    const renderTabs = (props) => {
        return (
            <Animated.View style={[styles.tabbar, { transform: [{ translateY: interpolatedHeight }] }]}>
                <TabBar
                    {...props}
                    indicatorStyle={{ backgroundColor: Colors.primaryText }}
                    labelStyle={{ color: Colors.primarydText }}
                    style={{ backgroundColor: Colors.white, height: TAB_BAR_HEIGHT }}
                />
            </Animated.View>
        );
    };

    return (
        <SafeAreaView style={{ flex: 1 }}>
            <TabView
                style={styles.tabStyle}
                navigationState={{ index, routes }}
                renderScene={renderScene}
                renderTabBar={renderTabs}
                onIndexChange={setIndex}
                initialLayout={initialLayout}
                swipeEnabled={false}
            />
        </SafeAreaView>
    );
};

ValidationScreen.propTypes = {
    navigation: PropTypes.object,
};

const styles = StyleSheet.create({
    tabbar: {
        marginTop: Platform.OS === 'android' ? NAV_BAR_HEIGHT_ANDROID : NAV_BAR_HEIGHT_IOS,
        position: 'absolute',
        top: 0,
        right: 0,
        left: 0,
        zIndex: 1000000,
    },
    tabStyle: {
        position: 'relative',
        flex: 1,
    },
});

export default React.memo(ValidationScreen);
