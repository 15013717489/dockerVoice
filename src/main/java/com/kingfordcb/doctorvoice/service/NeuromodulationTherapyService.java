package com.kingfordcb.doctorvoice.service;

import com.kingfordcb.doctorvoice.entity.LeftVoice;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 神经调控治疗service
 * @Author: lpf
 * @Date: 2023/06/07
 */

public interface NeuromodulationTherapyService {

    /**
     * 左耳处理
     */
    String leftear(LeftVoice leftVoice) throws FileNotFoundException;
}
