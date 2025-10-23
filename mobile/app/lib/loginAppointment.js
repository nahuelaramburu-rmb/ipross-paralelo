import SInfo from 'react-native-sensitive-info';

export const userLoginAppointment = async () => _userLoginAppointment();

import { apiUrls, loginKeys, loginIpross } from '../configs/api';

const _userLoginAppointment = async () => {
    const body = {
        email: loginIpross['user_api'],
        password: loginIpross['passw_api'],
    };

    let formData = [];

    for (let property in body) {
        var encodedKey = encodeURIComponent(property);
        var encodedValue = encodeURIComponent(body[property]);
        formData.push(encodedKey + '=' + encodedValue);
    }

    formData = formData.join('&');

    try {
        const response = await fetch(apiUrls['loginUserTurnos'], {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        const json = await response.json();

        if (!response.ok) {
            return { error: json };
        }

        const token = typeof json.access_token !== 'undefined' ? json.access_token : json.token;

        await SInfo.setItem('token', token, {});

        let authResponse = {
            ...json,
        };

        return authResponse;
    } catch (err) {
        console.log(err);
        return { error: err };
    }
};
