import Config from 'react-native-config';

//const host = '177a71b0.ngrok.io'; // local
const host = Config.HOST;
const turnos = Config.APITURNOS;
const maps = Config.API_GEOCODING;
const mapsKey = Config.API_GEOCODING_KEY;

const apiUrls = {
    api: 'https://' + host + '/validation-api/v1/',
    'identity-service': 'https://' + host + '/identity-service/v1/',
    'notification-service': 'https://' + host + '/notification-service/v1/',
    'report-server': 'http://' + host + '/report-server/v1/',
    'general-api': 'https://' + host + '/vemsalud/',
    'api-turnos': 'https://' + turnos,
    loginUserTurnos: 'https://' + turnos + '/apiv1/login?',
    'api-maps': maps + '?key=' + mapsKey + '&address=',
};

const loginKeys = {
    key: Config.LOGIN_KEY,
    secret: Config.LOGIN_SECRET,
};

const loginIpross = {
    user_api: Config.USER_API_IPROSS,
    passw_api: Config.PASSW_API_IPROSS,
};

const reportServerKeys = {
    key: Config.REPORTSERVER_KEY,
    secret: Config.REPORTSERVER_SECRET,
};

const codeSalt = {
    TOKEN_KEY: Config.TOKEN_KEY,
    QR_SECRET: Config.QR_SECRET,
    QR_IV: Config.QR_IV_SECRET,
};

export { apiUrls, reportServerKeys, codeSalt, loginKeys, loginIpross };
