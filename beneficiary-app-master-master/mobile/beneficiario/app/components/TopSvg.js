import React from 'react';
import Svg, { G, Defs, Rect, Path, Use, ClipPath } from 'react-native-svg';
import { verticalScale } from '../lib/size-normalizer';

const TopSvg = ({ style }) => {
    return (
        <Svg width="100%" height={verticalScale(220)} style={style} viewBox="0 0 79.441 38.12" enable-background="new 0 0 79.441 38.12" preserveAspectRatio="xMidYMin">
            <G>
                <Defs>
                    <Rect id="SVGID_1_" width="119.056" height="55.026" />
                </Defs>
                <ClipPath id="SVGID_2_">
                    <Use href="#SVGID_1_" overflow="visible" />
                </ClipPath>
                <G opacity="0.1" clip-path="url(#SVGID_2_)">
                    <G>
                        <Defs>
                            <Rect id="SVGID_3_" x="-86.572" y="-83.314" width="169.009" height="121.434" />
                        </Defs>
                        <ClipPath id="SVGID_4_">
                            <Use href="#SVGID_3_" overflow="visible" />
                        </ClipPath>
                        <Path clip-path="url(#SVGID_4_)" fill="#AAA79D" d="M48.425,9.559l-0.002-0.005c5.817,2.409,12.553,2.6,18.829-0.003 C79.8,4.352,85.757-10.038,80.556-22.587s-19.59-18.507-32.139-13.304c-2.483,1.028-4.484,2.651-6.416,4.315l-3.988,4.609 c-0.85,1.226-1.84,2.355-2.453,3.707L16.171,22.928C22.849,6.797,32.296,2.883,48.425,9.559" />
                        <G opacity="0.65" clip-path="url(#SVGID_4_)">
                            <G>
                                <Defs>
                                    <Rect id="SVGID_5_" x="-37.6" y="-32.04" width="73.159" height="70.16" />
                                </Defs>
                                <ClipPath id="SVGID_6_">
                                    <Use href="#SVGID_5_" overflow="visible" />
                                </ClipPath>
                                <Path clip-path="url(#SVGID_6_)" fill="#090609" d="M35.104-22.58C28.428-6.451,2.855-9.203,2.855-9.203 c-6.859-0.195-13.747-0.839-16.386-1.007c-15.035-0.959-21.896-5.681-24.069-21.829l6.662,48.836c0,0,0.762,3.975,1.659,6.137 c5.199,12.551,19.59,18.506,32.138,13.307c6.276-2.602,10.902-7.502,13.311-13.318l0.001,0.006L35.56-23.259 c-0.112,0.25-0.348,0.432-0.452,0.685L35.104-22.58z" />
                            </G>
                        </G>
                    </G>
                </G>
            </G>
        </Svg>
    );
};

export default React.memo(TopSvg);