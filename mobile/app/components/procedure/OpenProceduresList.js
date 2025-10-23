import React, { PureComponent } from 'react';
import { Text, View, StyleSheet, Dimensions, ActivityIndicator } from 'react-native';
import { FlatList } from 'react-native-gesture-handler';
import { connect } from 'react-redux';
import OpenProcedureCard from './OpenProcedureCard';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import strings from '../../constants/Strings';
import { searchNextPage } from '../../actions/procedureAction';

const { width, height } = Dimensions.get('screen');

class OpenProceduresList extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {};

        this._renderItem = this._renderItem.bind(this);
        this._renderEmptyList = this._renderEmptyList.bind(this);
        this._renderFooter = this._renderFooter.bind(this);
        this._searchNextPage = this._searchNextPage.bind(this);
    }

    _renderItem(item) {
        const { item: it } = item;
        return <OpenProcedureCard item={it} />;
    }

    _renderEmptyList() {
        return (
            <View style={styles.emptyListContainer}>
                <Text style={font_styles.secondary_text_bold}>{strings.openedProcedures.no_procedures}</Text>
            </View>
        );
    }

    _renderFooter() {
        let loading = (
            <View style={styles.containerCentered}>
                <ActivityIndicator size='large' color={Colors.primaryText} />
            </View>
        );

        if (this.props.loadingMoreProcedures) return loading;
        else return null;
    }

    _searchNextPage() {
        const { loadingMoreProcedures, searchNextPage } = this.props;
        if (loadingMoreProcedures) return;
        searchNextPage('opened');
    }

    _keyExtractor = (item, index) => item.id.toString();

    render() {
        const { procedures, style } = this.props;
        return (
            <View style={style}>
                <View style={styles.container}>
                    <FlatList
                        scrollEventThrottle={16}
                        data={procedures}
                        horizontal
                        style={styles.flatList}
                        ListEmptyComponent={this._renderEmptyList}
                        renderFooter={this._renderFooter}
                        onEndReached={this._searchNextPage}
                        snapToInterval={width}
                        decelerationRate='fast'
                        showsHorizontalScrollIndicator={false}
                        renderItem={this._renderItem}
                        keyExtractor={this._keyExtractor}
                    />
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        width: width,
        backgroundColor: Colors.appBackground,
    },
    emptyListContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
    },
    flatList: {
        flex: 1,
    },
});

function mapStateToProps(state) {
    return {
        loadingMoreProcedures: state.procedure.procedures.loadingMore,
    };
}

export default connect(mapStateToProps, {
    searchNextPage,
})(OpenProceduresList);
