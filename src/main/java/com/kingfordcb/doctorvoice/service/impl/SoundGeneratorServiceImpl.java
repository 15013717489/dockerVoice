package com.kingfordcb.doctorvoice.service.impl;

import com.kingfordcb.doctorvoice.entity.SoundTrack;
import com.kingfordcb.doctorvoice.service.SoundGeneratorService;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
public class SoundGeneratorServiceImpl implements SoundGeneratorService {

    public String AUDIO_PURETONE = "pureTone";//纯音
    public String AUDIO_PULSE = "pulse";//脉冲
    public String AUDIO_NOISE = "noise";//噪音
    public String AUDIO_GAUSSIANNOISE = "gaussianNoise";//高斯噪音
    public String AUDIO_EVENNOISE = "evenNoise";//均匀噪音
    public String AUDIO_MASKINGNOISE = "maskingNoise";//掩蔽噪音
    public String AUDIO_SWEEPTONE = "sweepTone";//啭音
    public String AUDIO_TONEPULSE = "tonePulse";//纯音脉冲
    public File generateSound(SoundTrack soundTrack,int duration,String fileName) {
        Instant startTime = Instant.now();
        File wavFile = null;
        try {
            float frequency1 = soundTrack.getFrequency1();// 频率 (Hz)
            float frequency2 = soundTrack.getFrequency2();// 频率 (Hz)
            final float sampleRate = 44100.0f;  // 采样率
            int amplitude = 32767;  // 振幅
            /*int volumePercent = soundTrack.getVolume(); // 获取音量因子百分比，范围为0-100
            float volume = volumePercent / 100.0f; // 将音量因子百分比转换为0-1之间的浮点数
            amplitude *= volume; // 将振幅乘以音量因子*/
            // 计算需要生成的总样本数
            int numSamples = (int) (sampleRate * duration);

            // 创建音频格式
            AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 2, true, false);

            String soundType = soundTrack.getSoundType();
            byte[] buffer = null;
            if (soundType.equals("pureTone")) {
                //纯音
                buffer = generatePureTone(sampleRate, frequency1, duration, amplitude);
            } else if (soundType.equals("pulse")) {
                //脉冲
                buffer = generatePulse(sampleRate, frequency1, duration, amplitude);
            } else if (soundType.equals("tonePulse")) {
                //纯音脉冲
                buffer = generateTonePulse(sampleRate, frequency1, frequency2, duration, amplitude);
            } else if (soundType.equals("whiteNoise")) {
                //噪声
                buffer = generateWhiteNoise(sampleRate, duration, amplitude);
            } else if (soundType.equals("gaussianNoise")) {
                //高斯噪声
                buffer = generateGaussianNoise(sampleRate, duration, amplitude);
            } else if (soundType.equals("uniformNoise")) {
                //均匀噪声
                buffer = generateUniformNoise(sampleRate, duration, amplitude);
            } else if (soundType.equals("maskingNoise")) {
                //掩蔽噪声
                //buffer = generateMaskingNoise(sampleRate, duration, amplitude);
            } else if (soundType.equals("sweepTone")) {
                //啭音
                buffer = generateSweepTone(sampleRate, frequency1, frequency2, duration, amplitude);
            }
            // 将缓冲区的音频数据保存到WAV文件
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, numSamples);
            Instant time1 = Instant.now();
            System.out.println(fileName+"音频生成成功花费时间：" + Duration.between(startTime, time1).toMillis());
            // 将音频数据保存到临时WAV文件
            wavFile = new File(fileName);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
            audioInputStream.close();
            Instant time2 = Instant.now();
            System.out.println(fileName+"音频持久化成功：" + wavFile.getAbsolutePath() + "花费时间：" + Duration.between(time1, time2).toMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return wavFile;
    }

    /**
     * 纯音脉冲
     * 纯音脉冲是脉冲和纯音的混合，可以理解为在每个脉冲周期内播放一个纯音。
     * @param sampleRate
     * @param pulseFrequency
     * @param toneFrequency
     * @param duration
     * @param amplitude
     * @return
     */
    private byte[] generateTonePulse(float sampleRate, float pulseFrequency, float toneFrequency, double duration, int amplitude) {
        int numSamples = (int) (duration * sampleRate);
        int samplesPerPulse = (int) (sampleRate / pulseFrequency);
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; ++i) {
            double time = i / sampleRate;
            double angle = 2.0 * Math.PI * toneFrequency * time;
            short value = 0;

            if (i % samplesPerPulse < samplesPerPulse / 2) {
                value = (short) (amplitude * Math.sin(angle));
            }

            output[2*i] = (byte) (value & 0xFF);
            output[2*i+1] = (byte) ((value >> 8) & 0xFF);
        }

        return output;
    }


    /**
     * 纯音
     * 纯音是由单一频率的振动生成的音频信号，而啭音通常是指在一定时间内其频率有规律地变化的音频信号
     * @param sampleRate
     * @param frequency
     * @param duration
     * @param amplitude
     * @return
     */
    public byte[] generatePureTone(float sampleRate, float frequency, double duration, int amplitude) {
        int numSamples = (int) (duration * sampleRate);
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; ++i) {
            double angle = 2.0 * Math.PI * frequency * i / sampleRate;
            short value = (short) (amplitude * Math.sin(angle));

            output[2*i] = (byte) (value & 0xFF);
            output[2*i+1] = (byte) ((value >> 8) & 0xFF);
        }
        return output;
    }

    /**
     * 脉冲
     * 将音频信号的幅度在特定的时间点设置为非零值，然后在其他时间保持零。你可以通过控制这些非零幅度的时间位置，频率和持续时间来生成各种类型的脉冲。
     * 脉冲音频在每个脉冲周期的开始都有一个非零幅度的样本，脉冲的频率由pulseFrequency决定。
     * @param sampleRate
     * @param pulseFrequency
     * @param duration
     * @param amplitude
     * @return
     */
    public byte[] generatePulse(float sampleRate, float pulseFrequency, double duration, int amplitude) {
        int numSamples = (int) (duration * sampleRate);
        int samplesPerPulse = (int) (sampleRate / pulseFrequency);
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; ++i) {
            short value = 0;

            if (i % samplesPerPulse == 0) {
                value = (short) amplitude;
            }

            output[2*i] = (byte) (value & 0xFF);
            output[2*i+1] = (byte) ((value >> 8) & 0xFF);
        }

        return output;
    }

    /**
     * 均匀噪声
     * 均匀噪声又称为矩形噪声
     */
    public byte[] generateUniformNoise(float sampleRate, int duration, int amplitude)  {
        Random random = new Random();
        int numSamples = (int) sampleRate * duration;
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (random.nextInt(2 * amplitude + 1) - amplitude);
            output[i * 2] = (byte) (sample & 0xFF);
            output[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return output;
    }

    /**
     * 高斯噪声
     * 高斯噪声又称为正态分布噪声
     */
    public  byte[] generateGaussianNoise(float sampleRate, double duration, int amplitude)  {
        Random RANDOM = new Random();
        int numSamples = (int) (duration * sampleRate);
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; ++i) {
            short value = (short) (RANDOM.nextGaussian() * amplitude);
            output[2 * i] = (byte) (value & 0xFF);
            output[2 * i + 1] = (byte) ((value >> 8) & 0xFF);
        }

        return output;
    }
    /**
     * 噪声
     * 又称白噪声
     */
    public byte[] generateWhiteNoise(float sampleRate, int duration, int amplitude) {
        Random random = new Random();
        int numSamples = (int) (duration * sampleRate);
        byte[] output = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (random.nextFloat() * 2 * amplitude - amplitude);
            output[i * 2] = (byte) (sample & 0xFF);
            output[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return output;
    }

    /**
     * 掩蔽噪声
     * 掩蔽噪声是一个音频处理技术，用于在有噪声的环境中改善语音的清晰度。
     * 未实现 JAVA实现起来很复杂
     */
    public byte[] maskingNoise(SourceDataLine line, float startFrequency, float endFrequency, int duration) {
        /*// 计算每个样本的频率变化率
        float frequencyChangeRate = (endFrequency - startFrequency) / (line.getFormat().getSampleRate() * duration);
        // 每次写入的帧数
        int bufferSize = (int) (line.getFormat().getFrameRate() * line.getFormat().getFrameSize());
        // 生成均匀噪声数据并写入输出线路
        byte[] buffer = new byte[bufferSize];
        int samplesWritten = 0;
        Random random = new Random();
        while (samplesWritten < line.getFormat().getSampleRate() * duration) {
            for (int i = 0; i < bufferSize; i += 2) {
                short sample = (short) (random.nextInt(Short.MAX_VALUE * 2 + 1) - Short.MAX_VALUE);
                short maskingSample = (short) (random.nextInt(Short.MAX_VALUE * 2 + 1) - Short.MAX_VALUE);

                short maskedSample = (short) (sample & maskingSample);

                buffer[i] = (byte) (maskedSample & 0xFF);
                buffer[i + 1] = (byte) (maskedSample >> 8 & 0xFF);

                samplesWritten++;
            }

            line.write(buffer, 0, bufferSize);
        }
        return buffer;*/
        return null;
    }

    /**
     * 啭音
     * 啭音需要明确的规则去改变频率。这个规则可以根据实际应用来设定，例如线性变化、指数变化、正弦变化等等。
     * 频率从startFrequency线性变化到endFrequency。
     * @param sampleRate
     * @param startFrequency
     * @param endFrequency
     * @param duration
     * @param amplitude
     * @return
     */

    public byte[] generateSweepTone(float sampleRate, float startFrequency, float endFrequency, double duration, int amplitude) {
            int numSamples = (int) (duration * sampleRate);
            byte[] output = new byte[numSamples * 2];

            for (int i = 0; i < numSamples; ++i) {
                double time = i / sampleRate;
                double frequency = startFrequency + (endFrequency - startFrequency) * time / duration;
                double angle = 2.0 * Math.PI * frequency * time;

                short value = (short) (amplitude * Math.sin(angle));

                output[2*i] = (byte) (value & 0xFF);
                output[2*i+1] = (byte) ((value >> 8) & 0xFF);
            }

            return output;
    }


}
