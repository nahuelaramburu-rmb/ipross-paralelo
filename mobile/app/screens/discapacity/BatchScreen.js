import React, { useCallback, useEffect } from 'react';
import { StyleSheet } from 'react-native';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import BatchItem from '../../components/BatchItem';
import ActiveBatchItem from '../../components/ActiveBatchItem';
import { posibleStatuses } from '../../lib/utils';
import { getBatches } from '../../actions/batchAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAnimatableHeader } from '../../hooks/utils';
import List from '../../components/list';

const BatchScreen = () => {
    const dispatch = useDispatch();
    const { onScroll } = useAnimatableHeader();

    const { batches, batchesLoading, loadingMoreBatches, batchesLinks } = useSelector(
        (state) => ({
            batches: state.batch.batches.items._embedded?.batches ?? [],
            batchesLoading: state.batch.batches.loading,
            loadingMoreBatches: state.batch.batches.loadingMore,
            batchesLinks: state.batch.batches.items._links ?? {},
        }),
        shallowEqual
    );

    const searchBatches = useCallback(
        (isRefresh = false, filters = null, link = null) => {
            return dispatch(getBatches(isRefresh, filters, link));
        },
        [dispatch]
    );

    useEffect(() => {
        searchBatches();
    }, [searchBatches]);

    const renderItem = ({ item: batchItem, index }) => {
        if (index === 0 && batchItem.status.name === posibleStatuses.ACTIVE) {
            console.log('aca');

            return <ActiveBatchItem item={batchItem} />;
        }
        return <BatchItem item={batchItem} />;
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <List
                onScroll={onScroll}
                loading={batchesLoading}
                loadingMore={loadingMoreBatches}
                renderItem={renderItem}
                data={batches}
                getData={searchBatches}
                links={batchesLinks}
            />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
    },
});

export default BatchScreen;
