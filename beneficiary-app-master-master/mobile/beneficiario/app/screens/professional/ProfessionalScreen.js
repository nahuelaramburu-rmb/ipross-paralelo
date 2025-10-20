import React, { PureComponent } from 'react';
import { StyleSheet, View, ActivityIndicator } from 'react-native';
import { connect } from 'react-redux';
import { SafeAreaView } from 'react-native-safe-area-context';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { moderateScale, verticalScale } from '../../lib/size-normalizer';
import ProfessionalForm from '../../components/professional/ProfessionalForm';
import { getTypesSpecialty } from '../../actions/professionalAction';
import * as Colors from '../../constants/Colors';

class ProfessionalScreen extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
        };
        this.state = {
            typesSpecialty: [],
        };
    }

    render() {
        const { loading } = this.state;

        if (loading) {
            return (
                <SafeAreaView style={styles.safeAreaView}>
                    <View style={styles.loadingContainer}>
                        <ActivityIndicator size='large' color={Colors.primaryText} />
                    </View>
                </SafeAreaView>
            );
        }

        return (
            <SafeAreaView style={styles.safeAreaView}>
                <KeyboardAwareScrollView
                    style={styles.container}
                    contentContainerStyle={styles.keyboardAwareContent}
                    keyboardShouldPersistTaps='always'
                    showVerticalScrollIndicator={false}
                    scrollEnabled={true}>
                    <ProfessionalForm
                        disabled={false}
                        onConfirm={this._onConfirm}
                        creationLoading={loading}
                        editing={true}
                    />
                </KeyboardAwareScrollView>
            </SafeAreaView>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    safeAreaView: {
        flex: 1,
    },
    loadingContainer: {
        flex: 1,
        alignContent: 'center',
    },
    procedureTitle: {
        paddingLeft: moderateScale(16),
        marginTop: verticalScale(16),
    },
    keyboardAwareContent: {
        flexGrow: 1,
        padding: moderateScale(16),
    },
});

function mapStateToProps(state) {
    return {
        typesSpecialty: state.professional.typesSpecialty.items ?? {},
        loadingtypesSpecialty: state.professional.typesSpecialty.loading,
    };
}

export default connect(mapStateToProps, {
    getTypesSpecialty,
})(ProfessionalScreen);
