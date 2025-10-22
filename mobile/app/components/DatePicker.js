import React, { useState, forwardRef, useImperativeHandle } from 'react';
import { View } from 'react-native';
import TextField from './TextField';
import * as Colors from '../constants/Colors';
import { moderateScale } from '../lib/size-normalizer';
import Icon from 'react-native-vector-icons/Ionicons';
import DateTimePicker from '@react-native-community/datetimepicker';
import { BaseButton } from 'react-native-gesture-handler';
import moment from 'moment';
import PropTypes from 'prop-types';

const DatePicker = forwardRef(({ value: initialValue, onChangeDate, ...props }, ref) => {
    const [value, setValue] = useState(initialValue || new Date());
    const [datePickerVisible, setDatePickerVisible] = useState(false);

    const handleOpenDatepicker = () => {
        setDatePickerVisible(true);
    };

    const handleDatePickerChange = (e, date) => {
        setDatePickerVisible(false);
        const selectedDate = date || value;
        setValue(selectedDate);
        if (onChangeDate) onChangeDate(selectedDate);
    };

    useImperativeHandle(ref, () => ({
        focus: handleOpenDatepicker,
    }));

    const today = moment(new Date()).format('D/M/YYYY');

    return (
        <>
            <BaseButton onPress={handleOpenDatepicker}>
                <View pointerEvents='box-only'>
                    <TextField
                        value={
                            moment(value).format('D/M/YYYY') === today ? '' : moment(value).format('D/M/YYYY')
                        }
                        editable={false}
                        placeholder='Seleccione un valor'
                        rightIcon={
                            <Icon name='ios-chevron-down' color={Colors.accent} size={moderateScale(20)} />
                        }
                        {...props}
                    />
                </View>
            </BaseButton>
            {datePickerVisible && (
                <DateTimePicker
                    testID='dateTimePicker'
                    value={value}
                    mode='date'
                    is24Hour={true}
                    display='spinner'
                    onChange={handleDatePickerChange}
                />
            )}
        </>
    );
});

DatePicker.propTypes = {
    value: PropTypes.object,
    onChangeDate: PropTypes.func,
};

export default DatePicker;
