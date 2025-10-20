import { UPDATING_CURRENT_SCENE } from '../constants/ActionTypes';

export const updateScene = (scene) => (dispatch) => _updateScene(scene, dispatch);
const _updateScene = (scene, dispatch) => {
    dispatch({ type: UPDATING_CURRENT_SCENE, scene: scene });
};
