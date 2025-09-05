import {
    FETCHING_TRIPS,
    FETCH_TRIPS_SUCCESS
} from '../constants/ActionTypes';

const trips = {
    trips: [{
        coordinates: [
            { latitude: -31.432047, longitude: -64.187045 },
            { latitude: -31.431515, longitude: -64.188704 },
            { latitude: -31.431130, longitude: -64.189831 },
            { latitude: -31.430947, longitude: -64.190421 },
            { latitude: -31.430819, longitude: -64.191000 },
            { latitude: -31.430892, longitude: -64.193318 },
            { latitude: -31.430892, longitude: -64.193318 },
            { latitude: -31.432119, longitude: -64.193833 },
            { latitude: -31.434508, longitude: -64.194852 }
        ],
        distance: '2,3 Kms',
        startTime: new Date(2019, 9, 12, 6, 12, 0),
        endTime: new Date(2019, 9, 12, 6, 30, 0),
        batch: 'Discapacidad',
        id: 1
    }, {
        coordinates: [
            { latitude: -31.423619, longitude: -64.185691 },
            { latitude: -31.422777, longitude: -64.184168 },
            { latitude: -31.423400, longitude: -64.182172 },
            { latitude: -31.423775, longitude: -64.181045 },
            { latitude: -31.424269, longitude: -64.179468 },
            { latitude: -31.424892, longitude: -64.177472 },
            { latitude: -31.425359, longitude: -64.174253 },
            { latitude: -31.428554, longitude: -64.171174 },
            { latitude: -31.430916, longitude: -64.167355 },
            { latitude: -31.432088, longitude: -64.163514 },
            { latitude: -31.431987, longitude: -64.160456 },
            { latitude: -31.430385, longitude: -64.159566 },
            { latitude: -31.427538, longitude: -64.158826 }
        ],
        distance: '4,01 Kms',
        startTime: new Date(2019, 9, 19, 20, 45, 0),
        endTime: new Date(2019, 9, 19, 20, 58, 0),
        batch: 'Discapacidad',
        id: 2
    }, {
        coordinates: [
            { latitude: -31.379999, longitude: -64.215215 },
            { latitude: -31.378315, longitude: -64.214572 },
            { latitude: -31.376465, longitude: -64.214207 },
            { latitude: -31.374682, longitude: -64.213439 },
            { latitude: -31.375103, longitude: -64.211486 },
            { latitude: -31.373060, longitude: -64.211078 },
            { latitude: -31.371457, longitude: -64.210735 },
            { latitude: -31.369213, longitude: -64.210574 },
            { latitude: -31.368361, longitude: -64.212763 },
            { latitude: -31.367491, longitude: -64.215134 },
            { latitude: -31.365888, longitude: -64.217516 },
            { latitude: -31.363598, longitude: -64.219769 }
        ],
        distance: '3,5 Kms',
        startTime: new Date(2019, 9, 25, 18, 6, 0),
        endTime: new Date(2019, 9, 25, 18, 20, 0),
        batch: 'Discapacidad',
        id: 3
    }, {
        coordinates: [
            { latitude: -31.419252, longitude: -64.132811 },
            { latitude: -31.420342, longitude: -64.133508 },
            { latitude: -31.421386, longitude: -64.133529 },
            { latitude: -31.422833, longitude: -64.133518 },
            { latitude: -31.423520, longitude: -64.132767 },
            { latitude: -31.423566, longitude: -64.129087 },
            { latitude: -31.423722, longitude: -64.124506 },
            { latitude: -31.423704, longitude: -64.119936 },
            { latitude: -31.423649, longitude: -64.117136 },
            { latitude: -31.424409, longitude: -64.115205 },
            { latitude: -31.426332, longitude: -64.114840 }
        ],
        distance: '7,6 Kms',
        startTime: new Date(2019, 9, 30, 15, 16, 0),
        endTime: new Date(2019, 9, 30, 15, 36, 0),
        batch: 'Discapacidad',
        id: 4
    }, {
        coordinates: [
            { latitude: -31.381978, longitude: -64.134153 },
            { latitude: -31.381694, longitude: -64.133327 },
            { latitude: -31.380164, longitude: -64.133370 },
            { latitude: -31.378589, longitude: -64.133284 },
            { latitude: -31.376656, longitude: -64.133177 },
            { latitude: -31.375630, longitude: -64.134110 },
            { latitude: -31.375685, longitude: -64.137543 },
            { latitude: -31.375630, longitude: -64.143154 }
        ],
        distance: '1,8 Kms',
        startTime: new Date(2019, 10, 5, 16, 32, 0),
        endTime: new Date(2019, 10, 6, 16, 47, 0),
        batch: 'Discapacidad',
        id: 5
    }]
};

export const getTrips = () => (dispatch) => _getTrips(dispatch);
const _getTrips = (dispatch) => {

    dispatch({ type: FETCHING_TRIPS });

    setTimeout(() => {
        dispatch({ type: FETCH_TRIPS_SUCCESS, trips: trips });
    }, 1200);

}