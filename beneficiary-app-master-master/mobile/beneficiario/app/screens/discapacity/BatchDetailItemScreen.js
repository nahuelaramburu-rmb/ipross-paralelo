import React, { useCallback, useEffect } from 'react';
import { Text, View, Dimensions } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { getBatchItems } from '../../actions/batchAction';
import List from '../../components/list';
import { useAnimatableHeader } from '../../hooks/utils';
import { StyleSheet } from 'react-native';
import { font_styles } from '../../lib/default-styles';
import * as Colors from '../../constants/Colors';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';

const periodTranslate = {
    DAILY: 'Diaria',
    WEEKLY: 'Semanal',
    MONTHLY: 'Mensual',
    YEARLY: 'Anual',
};

const { width } = Dimensions.get('screen');

const BatchDetailItemScreen = () => {
    const dispatch = useDispatch();
    const { onScroll } = useAnimatableHeader();

    const {
        batch,
        batchItemsRelatedId,
        batchItemsLoading,
        batchItems,
        batchItemsLink,
        batchItemsLoadingMore,
    } = useSelector(
        (state) => ({
            batch: state.batch.selectedBatch.item,
            batchItemsRelatedId: state.batch.selectedBatch.batchItems.batchId,
            batchItems: state.batch.selectedBatch.batchItems.items,
            batchItemsLink: state.batch.selectedBatch.batchItems.items._links ?? {},
            batchItemsLoading: state.batch.selectedBatch.batchItems.loading,
            batchItemsLoadingMore: state.batch.selectedBatch.batchItems.loadingMore,
        }),
        shallowEqual
    );

    const searchBatchItems = useCallback(
        (isRefresh = true, filters = null, link = batch._links ? batch._links.batchItems.href : null) => {
            return dispatch(getBatchItems(isRefresh, filters, link));
        },
        [dispatch, batch]
    );

    useEffect(() => {
        if (batch.id !== batchItemsRelatedId || !batchItems._embedded) {
            searchBatchItems();
        }
    }, [batch, batchItemsRelatedId, batchItems, searchBatchItems]);

    const renderItem = ({ item: item, index }) => {
        const { _embedded: embeddedContainer = {} } = item;
        const { medicalCenters = [], practitioners = [] } = embeddedContainer;
        return (
            <View style={[styles.itemCard]}>
                <View style={styles.cardHeader}>
                    <View style={styles.cardContent}>
                        <Text
                            numberOfLines={1}
                            ellipsizeMode='tail'
                            style={[font_styles.title_3_bold, { marginBottom: verticalScale(12) }]}>
                            {item.nomenclator.medicalPractice.name}
                        </Text>
                    </View>
                </View>
                <View style={styles.divider} />
                <View style={styles.cardDetail}>
                    <View style={styles.infoRow}>
                        <Text style={[font_styles.primary_text]}>{'Periodo: '}</Text>
                        <Text style={[font_styles.primary_text_bold]}>{periodTranslate[item.period]}</Text>
                    </View>
                    <View style={styles.infoRow}>
                        <Text style={[font_styles.primary_text]}>{'Cantidad asignada: '}</Text>
                        <Text style={[font_styles.primary_text_bold]}>{item.amount}</Text>
                    </View>
                </View>
                {medicalCenters.length !== 0 && (
                    <View style={[styles.cardHeader, { paddingVertical: verticalScale(4) }]}>
                        <Text style={[font_styles.primary_text]}>{'Centros Médicos: '}</Text>
                    </View>
                )}
                {medicalCenters.map((i) => {
                    return (
                        <View style={[styles.cardDetail]}>
                            <View style={styles.infoRow}>
                                <Text numberOfLines={1}>
                                    <Text style={[font_styles.primary_text_bold]}>{i.name}</Text>
                                </Text>
                            </View>
                        </View>
                    );
                })}
                {practitioners.length !== 0 && (
                    <View style={[styles.cardHeader, { paddingVertical: verticalScale(4) }]}>
                        <Text style={[font_styles.primary_text]}>{'Médicos: '}</Text>
                    </View>
                )}
                {practitioners.map((i) => {
                    return (
                        <View style={[styles.cardDetail]}>
                            <View style={styles.infoRow}>
                                <Text numberOfLines={1}>
                                    <Text
                                        style={[
                                            font_styles.primary_text_bold,
                                        ]}>{`Dr/a ${i.lastName}, ${i.name}`}</Text>
                                </Text>
                            </View>
                        </View>
                    );
                })}
            </View>
        );
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <List
                contentContainerStyle={styles.listContainer}
                contentOffset={{ x: 0, y: 0 }}
                onScroll={onScroll}
                loading={batchItemsLoading}
                loadingMore={batchItemsLoadingMore}
                renderItem={renderItem}
                data={batchItems._embedded ? batchItems._embedded.batchItems : []}
                getData={searchBatchItems}
                links={batchItemsLink}
            />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    itemCard: {
        flex: 1,
        backgroundColor: Colors.white,
        borderRadius: moderateScale(10),
        width: width - moderateScale(30),
        padding: moderateScale(12),
        marginBottom: verticalScale(14),
        elevation: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    cardHeader: {
        width: '100%',
        alignItems: 'center',
        justifyContent: 'flex-start',
        flexDirection: 'row',
    },
    cardContent: {
        flex: 0.9,
        alignItems: 'flex-start',
        justifyContent: 'center',
    },
    infoRow: {
        flexDirection: 'row',
        display: 'flex',
        alignItems: 'center',
    },
    cardDetail: {
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingVertical: verticalScale(4),
    },
    divider: {
        borderBottomColor: Colors.lightDividerLine,
        borderBottomWidth: 0.5,
        width: '100%',
        marginBottom: verticalScale(12),
    },
    safeArea: {
        flex: 1,
    },
    listContainer: {
        paddingTop: moderateScale(10),
    },
});

export default BatchDetailItemScreen;
