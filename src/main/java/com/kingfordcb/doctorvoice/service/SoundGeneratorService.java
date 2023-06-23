package com.kingfordcb.doctorvoice.service;

import com.kingfordcb.doctorvoice.entity.SoundTrack;

import java.io.File;
import java.io.FileNotFoundException;

public interface SoundGeneratorService {

    File generateSound(SoundTrack soundTrack, int audioDuration,String FileName);
}
