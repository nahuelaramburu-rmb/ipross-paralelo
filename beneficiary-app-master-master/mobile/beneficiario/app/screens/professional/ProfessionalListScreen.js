import React, { useCallback, useEffect } from 'react';
import { StyleSheet } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAnimatableHeader } from '../../hooks/utils';
import { getProfessionalSpecialty } from '../../actions/professionalAction';
import List from '../../components/list';
import ProfessionalItem from '../../components/professional/ProfessionalItem';
import PropTypes from 'prop-types';

const ProfessionalListScreen = ({ route }) => {

    const dispatch = useDispatch();

    const { onScroll } = useAnimatableHeader();

    const { practitioners, practitionersLoading, loadingMorePractitioners, practitionersLinks } = useSelector(
        (state) => ({
            practitioners: state.professional.items._embedded?.practitioners ?? [],
            practitionersLoading: state.professional.loading,
            loadingMorePractitioners: state.professional.loadingMore,
            practitionersLinks: state.professional.items._links ?? {},
        }),
        shallowEqual
    );
    
    const idSpecialty = route.params?.idSpecialty ?? null;
    const idtown = route.params?.idTown ?? null;

    const getProffesionals = useCallback(
        
        (isRefresh = false, filters = null, link = null) => {
            return dispatch(getProfessionalSpecialty(idSpecialty,idtown,isRefresh,link))
        },
        [dispatch]
    );

    useEffect(() => {
        getProffesionals();
    }, [getProffesionals]);


    const renderItem = ({ item: practitionerItem }) => {
        return <ProfessionalItem item={practitionerItem} />;
    };
    
    return (
        <SafeAreaView style={styles.safeArea}>
            <List
                onScroll={onScroll}
                loading={practitionersLoading}
                loadingMore={loadingMorePractitioners}
                renderItem={renderItem}
                data={practitioners}
                getData={getProffesionals}
                links={practitionersLinks}
            />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
});

ProfessionalListScreen.propTypes = {
    route: PropTypes.object,
};

export default ProfessionalListScreen;
