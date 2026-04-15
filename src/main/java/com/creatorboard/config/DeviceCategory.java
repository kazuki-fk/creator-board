package com.creatorboard.config;

import java.util.Arrays;
import java.util.List;

public class DeviceCategory {

    // ドラム・リズム系
    public static final List<String> DRUM = Arrays.asList(
        "Drum Rack",
        "Impulse",
        "DrumSynth"
    );

    // ミキシング・マスタリング系
    public static final List<String> MIXING = Arrays.asList(
        "Compressor",
        "Glue Compressor",
        "Multiband Dynamics",
        "Limiter",
        "EQ Eight",
        "EQ Three",
        "Saturator",
        "Vinyl Distortion",
        "Dynamic Tube",
        "Gate",
        "Expander",
        "Limiter"
        
    );

    // オーディオエフェクト系
    public static final List<String> AUDIO_FX = Arrays.asList(
        "Reverb",
        "Delay",
        "Echo",
        "Chorus",
        "Flanger",
        "Phaser",
        "Frequency Shifter",
        "Resonator",
        "Spectral Resonator",
        "Spectral Time",
        "Corpus",
        "Redux",
        "Erosion",
        "Filter",
        "Auto Filter",
        "Auto Pan",
        "Beat Repeat",
        "Looper",
        "Utility",
        "Stereo Enhancer",
        "Roar",
        "Hybrid Reverb",
        "Vocoder"
    );

    // インストゥルメント系（上記以外はすべてここに分類）
    public static final List<String> INSTRUMENT = Arrays.asList(
        "Wavetable",
        "Operator",
        "Analog",
        "Sampler",
        "Simpler",
        "Meld",
        "Drift",
        "Mangle",
        "Electric",
        "Tension",
        "Collision",
        "Instrument Rack",
        "External Instrument",
        "Granulator III"
    );

    public static String classify(String deviceName) {
        if (DRUM.contains(deviceName))     return "drum";
        if (MIXING.contains(deviceName))   return "mixing";
        if (AUDIO_FX.contains(deviceName)) return "fx";
        return "instrument";
    }
}