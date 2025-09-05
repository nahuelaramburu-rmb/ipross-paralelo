import React from 'react';
import Svg, { G, Defs, Rect, Path, Use, ClipPath } from 'react-native-svg';
import { verticalScale } from '../lib/size-normalizer';

const BottomSvg = ({ style }) => {
    return (
        <Svg width="100%" height={verticalScale(180)} style={style} viewBox="0 0 79.441 38.12" enable-background="new 0 0 79.441 38.12" preserveAspectRatio="xMidYMin" >
            <G>
                <Defs>
                    <Rect id="SVGID_1_" x="-39.258" y="-16.905" width="119.028" height="55.026" />
                </Defs>
                <ClipPath id="SVGID_2_">
                    <Use href="#SVGID_1_" overflow="visible" />
                </ClipPath>
                <G opacity="0.1" clip-path="url(#SVGID_2_)">
                    <G>
                        <Defs>
                            <Rect id="SVGID_3_" x="-2.667" width="169.009" height="121.435" />
                        </Defs>
                        <ClipPath id="SVGID_4_">
                            <Use href="#SVGID_3_" overflow="visible" />
                        </ClipPath>
                        <Path clip-path="url(#SVGID_4_)" fill="#AAA79D" d="M31.345,28.562l0.002,0.005c-5.817-2.409-12.553-2.6-18.828,0.003 c-12.55,5.2-18.507,19.589-13.305,32.138c5.201,12.549,19.589,18.507,32.138,13.305c2.484-1.029,4.485-2.652,6.417-4.316 l3.988-4.609c0.85-1.225,1.839-2.355,2.452-3.707l19.389-46.186C56.921,31.323,47.474,35.238,31.345,28.562" />
                        <G opacity="0.65" clip-path="url(#SVGID_4_)">
                            <G>
                                <Defs>
                                    <Rect id="SVGID_5_" x="44.209" width="73.159" height="70.16" />
                                </Defs>
                                <ClipPath id="SVGID_6_">
                                    <Use href="#SVGID_5_" overflow="visible" />
                                </ClipPath>
                                <Path clip-path="url(#SVGID_6_)" fill="#090609" d="M44.664,60.7c6.678-16.129,32.25-13.376,32.25-13.376 c6.859,0.195,13.746,0.838,16.385,1.006c15.035,0.959,21.896,5.682,24.07,21.83l-6.663-48.836c0,0-0.761-3.975-1.658-6.139 	c-5.2-12.55-19.59-18.505-32.138-13.306C70.633,4.482,66.008,9.381,63.6,15.199l-0.002-0.006L44.209,61.379 c0.113-0.25,0.349-0.432,0.453-0.684L44.664,60.7z" />
                            </G>
                        </G>
                    </G>
                </G>
            </G>
        </Svg>
    );
};

export default React.memo(BottomSvg);