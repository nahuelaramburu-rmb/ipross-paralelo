import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, View, FlatList, Dimensions } from 'react-native';
import { verticalScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';
import DateFilter from '../components/DateFilter';
import Button from '../components/Button';
import strings from '../constants/Strings';
import { CommonActions } from '@react-navigation/native';
import { SafeAreaView } from 'react-native-safe-area-context';

const { width, height } = Dimensions.get('screen');

export default class FilterModal extends Component {
    static propTypes = {
        navigation: PropTypes.shape({
            state: PropTypes.shape({
                params: PropTypes.shape({
                    filters: PropTypes.arrayOf(PropTypes.object).isRequired,
                    onSaveFilters: PropTypes.func.isRequired,
                }),
            }),
            setParams: PropTypes.func.isRequired,
            dispatch: PropTypes.func.isRequired,
        }),
    };

    constructor(props) {
        super(props);
        this.state = {
            filters: [],
        };

        this._renderItem = this._renderItem.bind(this);
        this._keyExtractor = this._keyExtractor.bind(this);
        this._onDateChange = this._onDateChange.bind(this);
        this._saveFilters = this._saveFilters.bind(this);
        this._clearFilters = this._clearFilters.bind(this);
    }

    _keyExtractor = (item, index) => item.key.toString();

    componentDidMount() {
        const filters = this.props.route.params?.filters ?? [];
        this.setState({ filters: filters });
        const filtersApplied = filters.filter((filter) => filter.value !== null);
        this.props.navigation.setParams({
            clearFilters: this._clearFilters,
            filtersApplied: filtersApplied.length,
        });
    }

    _clearFilters() {
        let { filters } = this.state;
        const { route } = this.props;
        filters.forEach((filter) => (filter.value = null));
        const onSaveFilters = route.params?.onSaveFilters ?? null;
        this.props.navigation.dispatch(CommonActions.goBack());
        if (onSaveFilters) onSaveFilters(filters);
    }

    _onDateChange(key, values) {
        const { filters } = this.state;
        const indx = filters.findIndex((fil) => fil.key === key);
        let updatedFilters = [...filters];
        updatedFilters[indx].value = values;
        this.setState({ filters: updatedFilters });
    }

    _renderItem({ item }) {
        switch (item.type) {
            case 'date':
                return (
                    <DateFilter
                        title={item.title}
                        description={item.description}
                        value={item.value}
                        onDateChange={(values) => this._onDateChange(item.key, values)}
                    />
                );
        }
    }

    _saveFilters() {
        const { route } = this.props;
        const onSaveFilters = route.params?.onSaveFilters ?? null;
        this.props.navigation.dispatch(CommonActions.goBack());
        if (onSaveFilters) onSaveFilters(this.state.filters);
    }

    render() {
        const { filters } = this.state;
        return (
            <SafeAreaView style={{ flex: 1 }}>
                <View style={styles.container}>
                    <FlatList
                        scrollEventThrottle={16}
                        data={filters}
                        contentContainerStyle={{ flexGrow: 1, width: width }}
                        renderItem={this._renderItem}
                        keyExtractor={this._keyExtractor}
                    />
                    <View style={styles.buttonContainer}>
                        <Button
                            title={strings.filters.apply}
                            block={true}
                            raised={true}
                            type='solid'
                            onPress={this._saveFilters}
                        />
                    </View>
                </View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        backgroundColor: Colors.appBackground,
    },
    buttonContainer: {
        height: verticalScale(64),
        paddingHorizontal: 24,
        width: width,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
    },
});
