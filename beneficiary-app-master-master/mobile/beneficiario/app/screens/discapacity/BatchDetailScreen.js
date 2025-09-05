import React, { PureComponent } from 'react';
import { Text, StyleSheet, View, ActivityIndicator, Dimensions } from 'react-native';
import * as Colors from '../../constants/Colors';
import { font_styles } from '../../lib/default-styles';
import { verticalScale, moderateScale } from '../../lib/size-normalizer';
import strings from '../../constants/Strings';
import moment from 'moment';
import get from 'lodash/get';
import { getStatusColor } from '../../lib/utils';
import { connect } from 'react-redux';
import { getBatch } from '../../actions/batchAction';
import { SafeAreaView } from 'react-native-safe-area-context';
import BatchDetailItemScreen from './BatchDetailItemScreen';

const { width, height } = Dimensions.get('screen');

class BatchDetailScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        const batchLink = this.props.route.params?.batchLink ?? null;
        const batchId = this.props.route.params?.batchId ?? null;
        if (!batchLink) return;
        if (batchId !== this.props.batch.id) this.props.getBatch(batchLink);
    }

    render() {
        const { batch, batchLoading } = this.props;

        if (batchLoading || !batch.id) {
            return (
                <SafeAreaView style={{ flex: 1 }}>
                    <View style={styles.loadingContainer}>
                        <ActivityIndicator color={Colors.primaryText} size='large' />
                        <Text style={[font_styles.secondary_text, { marginTop: verticalScale(5) }]}>
                            {strings.batches.loading_batch}
                        </Text>
                    </View>
                </SafeAreaView>
            );
        }

        return (
            <SafeAreaView style={{ flex: 1 }}>
                <View style={styles.container}>
                    <View style={styles.header}>
                        <View style={styles.headerContainer}>
                            <View style={styles.rowTitle}>
                                <Text style={font_styles.title_2}>{`Módulo N° ${batch.id}`}</Text>
                                <Text
                                    style={[
                                        font_styles.title_3_bold,
                                        { color: getStatusColor(get(batch, 'status.name', '')) },
                                    ]}>
                                    {get(batch, 'status.name', '-')}
                                </Text>
                            </View>
                            <View style={styles.descriptionItem}>
                                <Text style={[font_styles.primary_text, { marginRight: moderateScale(1) }]}>
                                    {'Validez'}:{' '}
                                </Text>
                                <Text
                                    style={[
                                        font_styles.primary_text_bold,
                                        { marginRight: moderateScale(3) },
                                    ]}>
                                    {moment(batch.dateFrom).format('D/M/YYYY')}
                                </Text>
                                <Text style={[font_styles.primary_text, { marginRight: moderateScale(1) }]}>
                                    {'-'}{' '}
                                </Text>
                                <Text style={[font_styles.primary_text_bold]}>
                                    {moment(batch.dateTo).format('D/M/YYYY')}
                                </Text>
                            </View>
                            <View
                                style={[
                                    styles.descriptionItem,
                                    { flexDirection: 'column', alignItems: 'flex-start' },
                                ]}>
                                <Text style={[font_styles.primary_text, { marginBottom: verticalScale(6) }]}>
                                    {'Diagnósticos'}:{' '}
                                </Text>
                                {get(batch, 'diagnosis', []).map((diag) => (
                                    <Text
                                        numberOfLines={2}
                                        key={diag.code}
                                        style={[font_styles.primary_text_bold]}>
                                        {`(${diag.code}) - ${diag.name}`}
                                    </Text>
                                ))}
                            </View>
                        </View>
                    </View>
                    <BatchDetailItemScreen />
                </View>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Colors.appBackground,
        alignItems: 'center',
    },
    loadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    header: {
        width: width - moderateScale(20),
        backgroundColor: Colors.white,
        borderRadius: moderateScale(8),
        marginTop: moderateScale(6),
        padding: moderateScale(18),
        elevation: 1,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 1,
        },
        shadowOpacity: 0.18,
        shadowRadius: 1.0,
    },
    headerContainer: {
        alignItems: 'flex-start',
        justifyContent: 'space-between',
    },
    descriptionItem: {
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'flex-start',
        paddingVertical: verticalScale(6),
    },
    rowTitle: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        paddingBottom: verticalScale(6),
    },
});

function mapStateToProps(state) {
    return {
        batch: state.batch.selectedBatch.item,
        batchLoading: state.batch.selectedBatch.loading,
    };
}

export default connect(mapStateToProps, {
    getBatch,
})(BatchDetailScreen);
