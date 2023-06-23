package com.kingfordcb.doctorvoice.entity;

import lombok.Data;

/**
 * 左耳实例
 * @Author: lpf
 * @Date: 2023/06/07
 */
@Data
public class LeftVoice {

    /**
     * 音量
     */
    private int volume;

    /**
     * 波幅
     */
    private int amplitude;

    /**
     * 峰值延迟
     */
    private int peakDelay;

    /**
     * 波谷值
     */
    private int troughValue;

    /**
     * 音频时长
     */
    private int audioDuration;

    /**
     * 子声道1
     */
    private SoundTrack soundTrack1;

    /**
     * 子声道2
     */
    private SoundTrack soundTrack2;

}
