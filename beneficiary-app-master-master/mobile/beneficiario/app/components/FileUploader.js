import React, { Component } from 'react';
import { View, StyleSheet, Text, Platform, ActivityIndicator } from 'react-native';
import { moderateScale, verticalScale } from '../lib/size-normalizer';
import * as Colors from '../constants/Colors';
import Icon from 'react-native-vector-icons/Ionicons';
import { font_styles } from '../lib/default-styles';
import ImagePicker from 'react-native-image-picker';
import { fileUploaderWithProgress } from '../lib/utils';
import { apiUrls } from '../configs/api';
import { DropdownHolder } from './DropDownHolder';
import strings from '../constants/Strings';
import { ScrollView, TouchableOpacity } from 'react-native-gesture-handler';

const options = {
    title: 'Seleccionar Imagen',
    cameraType: 'back',
    mediaType: 'photo',
    cancelButtonTitle: 'Cancelar',
    takePhotoButtonTitle: 'Tomar una foto',
    chooseFromLibraryButtonTitle: 'Seleccionar desde galería',
    quality: 0.8,
    maxWidth: 1200,
    maxHeight: 1200,
    storageOptions: {
        skipBackup: true,
        path: 'images',
    },
};

export default class FileUploader extends Component {
    constructor(props) {
        super(props);
        this.state = {
            currentPhotos: [],
        };

        this._selectFile = this._selectFile.bind(this);
        this._renderFiles = this._renderFiles.bind(this);
        this._updateProgress = this._updateProgress.bind(this);
        this._getExtension = this._getExtension.bind(this);
    }

    componentDidUpdate(prevProps) {
        if (this.props.files !== prevProps.files) {
            const processedFiles = this.props.files.map((file) => ({
                ...file,
                progress: 100,
                success: null,
            }));
            this.setState({ currentPhotos: processedFiles });
        }
    }

    _selectFile() {
        ImagePicker.showImagePicker(options, (response) => {
            console.log('Response = ', response);

            if (response.didCancel) {
                console.log('User cancelled image picker');
            } else if (response.error) {
                console.log('ImagePicker Error: ', response.error);
            } else if (response.customButton) {
                console.log('User tapped custom button: ', response.customButton);
            } else {
                let source;
                if (Platform.OS === 'android') {
                    source = { uri: response.uri, isStatic: true };
                } else {
                    source = { uri: response.uri.replace('file://', ''), isStatic: true };
                }

                const ext = this._getExtension(response.fileName);
                const filename = `App-Consultorio-${new Date().toISOString()}.${ext}`;
                const data = new FormData();
                data.append('file', {
                    uri: source.uri,
                    type: `image/${ext}`,
                    name: filename,
                });
                const url = apiUrls['api'] + `storage/reports?authorizationId=${this.props.relatedId}`;
                const opt = {
                    method: 'post',
                    headers: {},
                    body: data,
                };

                this.setState({
                    currentPhotos: [
                        ...this.state.currentPhotos,
                        { name: filename, progress: 0, success: null },
                    ],
                });

                fileUploaderWithProgress(url, opt, (progress) => this._updateProgress(filename, progress))
                    .then((res) => {
                        console.log(res);
                        if (res.status >= 200 && res.status <= 299)
                            this._updateFileUpdateStatus(filename, true);
                        else this._updateFileUpdateStatus(filename, false);
                    })
                    .catch((err) => {
                        console.log(err);
                        this._updateFileUpdateStatus(filename, false);
                        DropdownHolder.alert(
                            'error',
                            strings.common.error,
                            strings.common.image_cannot_be_uploaded
                        );
                    });
            }
        });
    }

    _getExtension(name) {
        return name.split('.').pop();
    }

    _updateFileUpdateStatus(filename, success) {
        const { currentPhotos } = this.state;
        const indx = currentPhotos.findIndex((ph) => ph.name === filename);
        if (indx === -1) return;
        const filesCopy = [...currentPhotos];
        filesCopy[indx].success = success;
        this.setState({ currentPhotos: filesCopy });
    }

    _updateProgress(filename, progress) {
        const { currentPhotos } = this.state;
        const indx = currentPhotos.findIndex((ph) => ph.name === filename);
        if (indx === -1) return;
        const filesCopy = [...currentPhotos];
        filesCopy[indx].progress = (progress.loaded / progress.total) * 100;
        this.setState({ currentPhotos: filesCopy });
    }

    _renderFiles(file, indx) {
        const progress = file.progress;
        let icon = null;
        if (file.success === null)
            icon = (
                <Icon
                    name={'md-checkmark-circle-outline'}
                    size={moderateScale(20)}
                    color={Colors.lightDividerLine}
                />
            );
        else if (file.success === true)
            icon = (
                <Icon name={'md-checkmark-circle-outline'} size={moderateScale(20)} color={Colors.primary} />
            );
        else
            icon = (
                <Icon
                    name={'md-close-circle-outline'}
                    size={moderateScale(20)}
                    color={Colors.statusRejected}
                />
            );
        return (
            <View style={styles.fileState} key={indx}>
                <View style={styles.fileName}>
                    <Text numberOfLines={1} ellipsizeMode='tail' style={[font_styles.subtitle]}>
                        {file.name}
                    </Text>
                </View>
                <View style={styles.fileStatusBar}>
                    <View style={styles.statusBar}>
                        <View
                            style={{
                                backgroundColor: Colors.accent,
                                width: `${progress}%`,
                                height: '100%',
                                borderRadius: moderateScale(4),
                            }}
                        />
                    </View>
                    <View style={styles.statusBarIcon}>{icon}</View>
                </View>
            </View>
        );
    }

    render() {
        const { currentPhotos } = this.state;
        const { loading_files } = this.props;
        let files = null;

        if (!loading_files) {
            if (currentPhotos.length === 0) {
                files = (
                    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                        <Text style={[font_styles.subtitle]}>{strings.fileUploader.no_files_uploaded}</Text>
                    </View>
                );
            } else {
                files = currentPhotos.map((file, index) => this._renderFiles(file, index));
            }
        } else {
            files = (
                <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                    <ActivityIndicator size='large' color={Colors.primaryText} />
                </View>
            );
        }

        return (
            <View style={styles.container}>
                <View style={styles.titleContainer}>
                    <Text style={font_styles.title_3}>{strings.fileUploader.files}</Text>
                </View>
                <View style={styles.contentContainer}>
                    <View style={styles.uploader}>
                        <TouchableOpacity
                            style={styles.uploadButton}
                            onPress={this._selectFile}
                            disabled={true}>
                            <Icon
                                name='md-cloud-upload'
                                color={Colors.darkDividerLine}
                                size={moderateScale(46)}
                            />
                            <Text style={[font_styles.subtitle]}>{strings.fileUploader.upload_file}</Text>
                        </TouchableOpacity>
                    </View>
                    <View style={styles.files}>
                        <ScrollView
                            scrollEventThrottle={16}
                            contentContainerStyle={{ flexGrow: 1 }}
                            style={{ flex: 1 }}
                            nestedScrollEnabled={true}>
                            {files}
                        </ScrollView>
                    </View>
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        width: '100%',
        flexDirection: 'column',
        padding: moderateScale(12),
    },
    contentContainer: {
        flexDirection: 'row',
        width: '100%',
        flex: 0.8,
    },
    titleContainer: {
        flexDirection: 'row',
        width: '100%',
        flex: 0.2,
        alignItems: 'center',
        justifyContent: 'flex-start',
    },
    uploader: {
        flex: 0.4,
        alignItems: 'center',
        justifyContent: 'center',
    },
    files: {
        flex: 0.6,
        alignItems: 'center',
        justifyContent: 'center',
    },
    uploadButton: {
        width: moderateScale(120),
        height: moderateScale(120),
        borderRadius: moderateScale(60),
        borderWidth: 0.5,
        borderStyle: 'dashed',
        borderColor: Colors.lightDividerLine,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
    },
    fileState: {
        width: '100%',
        height: verticalScale(38),
        flexDirection: 'column',
        alignItems: 'flex-start',
        justifyContent: 'space-around',
        marginVertical: moderateScale(6),
    },
    fileName: {
        flex: 0.4,
        /*alignItems: 'flex-start',
        justifyContent: 'center'*/
    },
    fileStatusBar: {
        width: '100%',
        flex: 0.6,
        flexDirection: 'row',
        alignItems: 'flex-start',
        justifyContent: 'center',
    },
    statusBar: {
        flex: 0.8,
        height: verticalScale(8),
        marginTop: moderateScale(6),
    },
    statusBarIcon: {
        flex: 0.2,
        alignItems: 'center',
        justifyContent: 'center',
    },
});
