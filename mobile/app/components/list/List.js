import React, { useEffect, useState, memo } from 'react';
import { Animated, Platform, StyleSheet, View, ActivityIndicator, Text } from 'react-native';
import { NAV_BAR_HEIGHT_ANDROID, NAV_BAR_HEIGHT_IOS } from '../header/HeaderWrapper';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import strings from '../../constants/Strings';
import { makeQueryFilters } from '../../lib/utils';
import PropTypes from 'prop-types';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';

const hasFiltersApplied = (filters) => {
    return filters.every((fil) => fil.value !== null);
};

const List = ({
    loadingMore,
    emptyText = strings.general.no_data,
    renderItem,
    data,
    filters = [],
    getData,
    loading,
    links,
    onScroll,
    progressViewOffset = NAV_BAR_HEIGHT_ANDROID,
    contentInset = { top: NAV_BAR_HEIGHT_IOS },
    contentOffset = { x: 0, y: -NAV_BAR_HEIGHT_IOS },
    contentContainerStyle,
    loadingContainerStyle,
}) => {
    const [isRefreshing, setIsRefreshing] = useState(false);

    const searchNextPage = () => {
        if (loadingMore) return;

        const nextPage = links.next?.href ?? null;
        if (!nextPage) return;
        if (searchNextPage) getData(false, null, nextPage);
    };

    const renderEmptyList = () => {
        return (
            <View style={styles.emptyListContainer}>
                <Text style={[font_styles.secondary_text, { color: Colors.secondaryText }]}>{emptyText}</Text>
            </View>
        );
    };

    const renderFooter = () => {
        let loading = (
            <View style={styles.containerCentered}>
                <ActivityIndicator size='large' color={Colors.primaryText} />
            </View>
        );

        if (loadingMore) return loading;
        else return null;
    };

    const handleRefresh = () => {
        setIsRefreshing(true);
        const hasFilters = hasFiltersApplied(filters);
        let query = null;
        if (hasFilters) query = makeQueryFilters(filters);
        if (getData) {
            getData(true, query).finally(() => setIsRefreshing(false));
        }
    };

    useEffect(() => {
        if (filters.length > 0) {
            let filtersApplied = filters.filter((fil) => fil.value !== null);
            let query = null;
            if (filtersApplied.length > 0) query = makeQueryFilters(filters);
            getData(false, query);
        }
    }, [filters, getData]);

    return (
        <View style={styles.container}>
            {loading ? (
                <View style={loadingContainerStyle}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                    <Text style={[font_styles.secondary_text, { marginTop: verticalScale(6) }]}>
                        {strings.general.searching_data}
                    </Text>
                </View>
            ) : (
                <Animated.FlatList
                    onScroll={onScroll}
                    contentContainerStyle={[styles.flatlistWrapper, contentContainerStyle]}
                    style={styles.flatList}
                    data={data}
                    renderItem={(item) => renderItem(item)}
                    keyExtractor={(item) => item.id.toString()}
                    onRefresh={handleRefresh}
                    horizontal={false}
                    refreshing={isRefreshing}
                    onEndReachedThreshold={0.1}
                    scrollEventThrottle={16}
                    ListEmptyComponent={renderEmptyList}
                    onEndReached={searchNextPage}
                    ListFooterComponent={renderFooter}
                    contentInset={contentInset}
                    contentOffset={contentOffset}
                    progressViewOffset={progressViewOffset} // only works on android
                />
            )}
        </View>
    );
};

List.propTypes = {
    loadingMore: PropTypes.bool,
    searchNextPage: PropTypes.func,
    emptyText: PropTypes.string,
    renderItem: PropTypes.func,
    data: PropTypes.array,
    filters: PropTypes.array,
    getData: PropTypes.func,
    loading: PropTypes.bool,
    links: PropTypes.object,
    onScroll: PropTypes.object,
    progressViewOffset: PropTypes.number,
    contentInset: PropTypes.object,
    contentOffset: PropTypes.object,
    contentContainerStyle: PropTypes.object,
    loadingContainerStyle: PropTypes.object,
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    flatlistWrapper: {
        flexGrow: 1,
        paddingTop: Platform.OS === 'ios' ? 0 : NAV_BAR_HEIGHT_ANDROID,
    },
    flatList: {
        flex: 1,
        width: '100%',
    },
    emptyListContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    containerCentered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingVertical: moderateScale(16),
    },
});

export default memo(List);
