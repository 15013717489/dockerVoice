package com.kingfordcb.doctorvoice.entity;

import lombok.Data;

/**
 * 子声道实例
 * @Author: lpf
 * @Date: 2023/06/07
 */
@Data
public class SoundTrack {

    /**
     * 音量
     */
    private int volume;

    /**
     * 频率1
     */
    private float frequency1;

    /**
     * 频率2
     */
    private float frequency2;

    /**
     * 声音类型
     */
    private String soundType;

}
