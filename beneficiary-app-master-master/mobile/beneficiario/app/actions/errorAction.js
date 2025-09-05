import { CLEAR_ERROR, ERROR_REPORTERED, ERROR } from '../constants/ActionTypes';

import { apiUrls, reportServerKeys } from '../configs/api';
import DeviceInfo from 'react-native-device-info';
import base64 from 'base-64';

export const clearErrors = () => (dispatch) => _clearErrors(dispatch);
const _clearErrors = (dispatch) => {
    dispatch({ type: CLEAR_ERROR });
};

export const sendError = (err) => (dispatch) => _sendError(err, dispatch);
const _sendError = (err, dispatch) => {
    dispatch({ type: ERROR, error: { error: err }, notifiy: false });
};

export const reportErrors = () => (dispatch, getState) => _reportErrors(dispatch, getState);
const _reportErrors = async (dispatch, getState) => {
    const selectedUser = getState().profile.relatives.selectedUser;
    const credentials = getState().profile.credentials;
    const errors = getState().error.data;

    if (errors.length === 0 || Object.keys(selectedUser).length === 0) return;

    const dataToSend = {
        device_info: {
            brand: DeviceInfo.getBrand(),
            os_name: DeviceInfo.getSystemName(),
            os_version: DeviceInfo.getSystemVersion(),
            carrier: await DeviceInfo.getCarrier(),
            manufacturer: await DeviceInfo.getManufacturer(),
            model: DeviceInfo.getModel(),
        },
        user: {
            user_remote_id: selectedUser.id,
            name: selectedUser.name,
            surname: selectedUser.lastName,
            username: credentials.username,
        },
        app: {
            app_code_identifier: 'BeAppVEM',
        },
        report: {
            app_version: DeviceInfo.getVersion(),
            app_api_level: await DeviceInfo.getApiLevel(),
            app_last_update_time: await DeviceInfo.getLastUpdateTime(),
            errors: errors,
        },
    };

    try {
        const mac = await DeviceInfo.getMacAddress();
        dataToSend.device_info['mac'] = mac;

        const response = await fetch(apiUrls['report-server'] + 'report', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization:
                    'Basic ' + base64.encode(reportServerKeys['key'] + ':' + reportServerKeys['secret']),
            },
            body: JSON.stringify(dataToSend),
        });

        const json = await response.json();

        dispatch({ type: ERROR_REPORTERED, data: json });
    } catch (err) {
        console.log(err);
    }
};
