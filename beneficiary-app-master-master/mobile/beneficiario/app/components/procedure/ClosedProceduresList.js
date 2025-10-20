import React, { PureComponent } from 'react';
import { Text, View, StyleSheet, Dimensions, SectionList, ActivityIndicator } from 'react-native';
import { connect } from 'react-redux';
import * as Colors from '../../constants/Colors';
import ClosedProcedureItem from './ClosedProcedureItem';
import { font_styles } from '../../lib/default-styles';
import { moderateScale } from '../../lib/size-normalizer';
import strings from '../../constants/Strings';
import { getProcedures, searchNextPage } from '../../actions/procedureAction';

const { width } = Dimensions.get('screen');

class ClosedProceduresList extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            isFetching: false,
        };

        this._normaliceData = this._normaliceData.bind(this);
        this._renderSectionItem = this._renderSectionItem.bind(this);
        this._renderEmptyList = this._renderEmptyList.bind(this);
        this._renderFooter = this._renderFooter.bind(this);
        this._updateProcedures = this._updateProcedures.bind(this);
    }

    _keyExtractor = (item, index) => item.id.toString();

    componentDidUpdate(prevProps) {}

    _renderEmptyList() {
        return (
            <View style={styles.emptyListContainer}>
                <Text style={font_styles.secondary_text_bold}>{strings.closedProcedures.no_procedures}</Text>
            </View>
        );
    }

    _normaliceData(procedures) {
        const data = [];
        for (let procedure of procedures) {
            const indx = data.findIndex((item) => item.title.id === procedure.beneficiary.id);
            if (indx === -1) {
                data.push({ title: procedure.beneficiary, data: [procedure] });
            } else {
                data[indx].data.push(procedure);
            }
        }
        return data;
    }

    _renderSectionItem(item) {
        const { item: it } = item;
        return <ClosedProcedureItem item={it} />;
    }

    _renderSectionHeader({ section }) {
        return (
            <View style={styles.sectionHeader}>
                <Text
                    style={
                        font_styles.title_2_header
                    }>{`${section.title.name} ${section.title.lastName}`}</Text>
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

    _updateProcedures() {
        this.setState({ isFetching: true });
        this.props.getProcedures(true).finally(() => this.setState({ isFetching: false }));
    }

    _searchNextPage() {
        const { loadingMoreProcedures, searchNextPage } = this.props;
        if (loadingMoreProcedures) return;
        searchNextPage('closed');
    }

    render() {
        const { procedures, style } = this.props;
        const { isFetching } = this.state;
        return (
            <View style={style}>
                <View style={styles.container}>
                    <SectionList
                        stickySectionHeadersEnabled={true}
                        refreshing={isFetching}
                        onEndReachedThreshold={0.1}
                        onRefresh={() => this._updateProcedures()}
                        ListFooterComponent={() => this._renderFooter()}
                        renderSectionHeader={this._renderSectionHeader}
                        ListEmptyComponent={this._renderEmptyList}
                        contentContainerStyle={styles.sectionList}
                        renderItem={this._renderSectionItem}
                        keyExtractor={(item, index) => item + index}
                        sections={this._normaliceData(procedures)}
                        onEndReached={() => this._searchNextPage()}
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
    sectionHeader: {
        padding: moderateScale(16),
        backgroundColor: Colors.appBackground,
    },
    containerCentered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingVertical: moderateScale(16),
    },
    sectionList: {
        flexGrow: 1,
    },
    emptyListContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        width: width,
    },
});

function mapStateToProps(state) {
    return {
        loadingMoreProcedures: state.procedure.procedures.loadingMore,
    };
}

export default connect(mapStateToProps, {
    getProcedures,
    searchNextPage,
})(ClosedProceduresList);
