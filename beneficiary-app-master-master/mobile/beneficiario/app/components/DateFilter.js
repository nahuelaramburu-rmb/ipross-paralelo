import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { StyleSheet, Text, View, TouchableOpacity } from 'react-native';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';
import { font_styles } from '../lib/default-styles';
import moment from 'moment';
import Modal from 'react-native-modal';
import { Calendar } from 'react-native-calendario';
import Button from './Button';
import strings from '../constants/Strings';
import { regular } from '../constants/Fonts';

const height = require('react-native-extra-dimensions-android').get('REAL_WINDOW_HEIGHT');

const STARTING_DATE = '2020-01-01';

export default class DateFilter extends Component {
    static propTypes = {
        title: PropTypes.string.isRequired,
        description: PropTypes.string,
        value: PropTypes.object,
        onDateChange: PropTypes.func.isRequired,
    };

    static defaultProps = {
        color: Colors.primary,
        text: '',
        useNative: true,
    };

    constructor(props) {
        super(props);
        this.state = {
            modalDatesVisible: false,
            values: {
                startDate: this.props.value !== null ? this.props.value.startDate : null,
                endDate: this.props.value !== null ? this.props.value.endDate : null,
            },
        };

        this.calendarRef = null;
        this._showModalDates = this._showModalDates.bind(this);
        this._applyFilter = this._applyFilter.bind(this);
        this._getNumbersOfMonthsUntilNow = this._getNumbersOfMonthsUntilNow.bind(this);
    }

    componentDidUpdate(prevProps, prevState) {
        if (this.state.modalDatesVisible !== prevState.modalDatesVisible && this.state.modalDatesVisible) {
            if (this.calendarRef) {
                setTimeout(() => this.calendarRef.scrollToEnd({ animated: false }), 0);
            }
        }
    }

    _showModalDates() {
        this.setState({ modalDatesVisible: true });
    }

    _applyFilter(values) {
        if (values.startDate && values.endDate) this.setState({ values: values });
    }

    _confirmSelection() {
        const { values } = this.state;
        if (!values.startDate || !values.endDate) return;
        this.setState({ modalDatesVisible: false }, () => {
            this.props.onDateChange(values);
        });
    }

    _getNumbersOfMonthsUntilNow() {
        return Math.round(moment().add(1, 'month').diff(moment(STARTING_DATE), 'months', true));
    }

    render() {
        const { values } = this.state;
        return (
            <View style={styles.container}>
                <View style={styles.row}>
                    <Text style={font_styles.title_3_bold}>{this.props.title}</Text>
                    <TouchableOpacity onPress={() => this._showModalDates()} style={styles.datesCard}>
                        <View
                            style={{
                                flex: 0.5,
                                alignItems: 'center',
                                justifyContent: 'space-around',
                                borderRightColor: Colors.lightDividerLine,
                                borderRightWidth: 0.5,
                                paddingVertical: verticalScale(5),
                            }}>
                            <Text style={[font_styles.primary_text, { color: Colors.secondaryText }]}>
                                {strings.general.from}
                            </Text>
                            <Text style={[font_styles.primary_text, { color: Colors.accent }]}>
                                {values.startDate !== null
                                    ? moment(values.startDate).format('LL')
                                    : strings.general.no_selection}{' '}
                            </Text>
                        </View>
                        <View
                            style={{
                                flex: 0.5,
                                alignItems: 'center',
                                justifyContent: 'space-around',
                                paddingVertical: verticalScale(5),
                            }}>
                            <Text style={[font_styles.primary_text, { color: Colors.secondaryText }]}>
                                {strings.general.to}
                            </Text>
                            <Text style={[font_styles.primary_text, { color: Colors.accent }]}>
                                {values.endDate
                                    ? moment(values.endDate).format('LL')
                                    : strings.general.no_selection}{' '}
                            </Text>
                        </View>
                    </TouchableOpacity>
                </View>
                <Modal
                    isVisible={this.state.modalDatesVisible}
                    useNativeDriver={true}
                    onBackButtonPress={() => this.setState({ modalDatesVisible: false })}
                    onBackdropPress={() => this.setState({ modalDatesVisible: false })}
                    animationInTiming={500}
                    animationOutTiming={500}
                    backdropTransitionInTiming={500}
                    backdropTransitionOutTiming={500}
                    deviceHeight={height}
                    hideModalContentWhileAnimating={true}
                    animationOut={'slideOutDown'}
                    animationIn={'slideInUp'}>
                    <View style={styles.modalBackground}>
                        <Calendar
                            calendarListRef={(ref) => (this.calendarRef = ref)}
                            locale='es'
                            startDate={values.startDate}
                            endDate={values.endDate}
                            monthHeight={370}
                            minDate={STARTING_DATE}
                            maxDate={moment().add(1, 'month').format('YYYY-MM-DD')}
                            numberOfMonths={this._getNumbersOfMonthsUntilNow()}
                            startingMonth={STARTING_DATE}
                            initialListSize={4}
                            theme={THEME}
                            onChange={this._applyFilter}
                        />
                        <Button
                            title={strings.filters.save}
                            block={true}
                            Comp={TouchableOpacity}
                            raised={true}
                            type='solid'
                            onPress={() => this._confirmSelection()}
                        />
                    </View>
                </Modal>
            </View>
        );
    }
}

const THEME = {
    activeDayColor: {},
    monthTitleTextStyle: {
        color: Colors.primaryText,
        fontWeight: '300',
        fontSize: 16,
        fontFamily: regular,
    },
    emptyMonthContainerStyle: {},
    emptyMonthTextStyle: {
        fontWeight: '200',
        fontFamily: regular,
    },
    weekColumnsContainerStyle: {},
    weekColumnStyle: {
        paddingVertical: 10,
    },
    weekColumnTextStyle: {
        color: '#b6c1cd',
        fontSize: 13,
        fontFamily: regular,
    },
    nonTouchableDayContainerStyle: {},
    nonTouchableDayTextStyle: {},
    startDateContainerStyle: {},
    endDateContainerStyle: {},
    dayContainerStyle: {},
    dayTextStyle: {
        color: '#455A64',
        fontWeight: '200',
        fontSize: 15,
        fontFamily: regular,
    },
    dayOutOfRangeContainerStyle: {},
    dayOutOfRangeTextStyle: {},
    todayContainerStyle: {},
    todayTextStyle: {
        color: Colors.accent,
        fontFamily: regular,
    },
    activeDayContainerStyle: {
        backgroundColor: Colors.primary,
    },
    activeDayTextStyle: {
        color: Colors.primaryText,
    },
    nonTouchableLastMonthDayTextStyle: {},
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        backgroundColor: Colors.appBackground,
    },
    row: {
        height: verticalScale(200),
        flexDirection: 'column',
        justifyContent: 'space-around',
        alignItems: 'center',
        paddingHorizontal: moderateScale(24),
        paddingVertical: verticalScale(12),
    },
    filterTitle: {
        flex: 1,
        flexDirection: 'column',
        alignItems: 'flex-start',
        justifyContent: 'center',
        marginLeft: moderateScale(8),
    },
    datesCard: {
        flex: 0.6,
        borderColor: Colors.lightDividerLine,
        shadowColor: Colors.primaryText,
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.23,
        shadowRadius: 2.62,

        elevation: 4,
        borderRadius: moderateScale(6),
        width: '100%',
        backgroundColor: Colors.white,
        flexDirection: 'row',
        padding: moderateScale(8),
    },
    modalBackground: {
        backgroundColor: Colors.white,
        padding: moderateScale(10),
        borderRadius: moderateScale(4),
        borderColor: Colors.lightDividerLine,
        flex: 0.7,
    },
});
