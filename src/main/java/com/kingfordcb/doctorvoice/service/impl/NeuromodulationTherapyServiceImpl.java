package com.kingfordcb.doctorvoice.service.impl;


import com.kingfordcb.doctorvoice.entity.LeftVoice;
import com.kingfordcb.doctorvoice.entity.SoundTrack;
import com.kingfordcb.doctorvoice.service.NeuromodulationTherapyService;
import com.kingfordcb.doctorvoice.service.SoundGeneratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import javax.sound.sampled.*;
import java.io.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * 神经调控治疗service
 * @Author: lpf
 * @Date: 2023/06/07
 */
@Service
public class NeuromodulationTherapyServiceImpl implements NeuromodulationTherapyService {


    @Autowired
    private SoundGeneratorService soundGeneratorService;

    /**
     * 左耳处理
     */
    public String leftear(LeftVoice leftVoice) {
        File[] mavFiles = new File[2];
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        File output = new File( "output.wav" );
        //处理音轨1
        if(leftVoice.getSoundTrack1()!=null) {
            SoundTrack soundTrack1 = leftVoice.getSoundTrack1();
            futureList.add(CompletableFuture.runAsync(() -> {
                mavFiles[0] = soundGeneratorService.generateSound(soundTrack1, leftVoice.getAudioDuration(), "mavFile1.wav");
            }));
        }
        //处理音轨2
        if(leftVoice.getSoundTrack2()!=null){
            SoundTrack soundTrack2=leftVoice.getSoundTrack2();
            futureList.add(CompletableFuture.runAsync(() -> {
                mavFiles[1] = soundGeneratorService.generateSound(soundTrack2, leftVoice.getAudioDuration(), "mavFile2.wav");
            }));
        }
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join(); // 等待所有音轨处理完成
        String[] audioFiles = new String[2];
        String[] individualVolumes = new String[2];
        // 创建混音
        if (mavFiles[0] != null && mavFiles[1] != null) {
            audioFiles[0] = mavFiles[0].getPath();
            audioFiles[1] = mavFiles[1].getPath();
            individualVolumes[0] = decimalFormat.format(leftVoice.getSoundTrack1().getVolume() / 100.0f);
            individualVolumes[1] = decimalFormat.format(leftVoice.getSoundTrack2().getVolume() / 100.0f);
            ffmpegMixer(leftVoice.getVolume(), audioFiles, individualVolumes, output.getPath());
        } else if (mavFiles[0] != null) {
            output = mavFiles[0];
        } else if (mavFiles[1] != null) {
            output = mavFiles[1];
        }

        return output.getPath();
    }


    /**
     * 通过ffmpeg混音
     * @param allVoice 总音量
     * @param audioFiles 文件名
     * @param individualVolumes 文件音量
     * @param outputFilePath 输出路径
     */
    public void ffmpegMixer(int allVoice,String[] audioFiles,String[] individualVolumes,String outputFilePath){
        String ffmpegPath = "ffmpeg"; // FFmpeg 可执行文件的路径
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String volume = decimalFormat.format(allVoice / 100.0f); // 总体音量调整值，范围为 0.0 到 1.0

        try {
            StringBuilder filterComplex = new StringBuilder();
            StringBuilder inputs = new StringBuilder();

            for(int i = 0; i < audioFiles.length; i++) {
                filterComplex.append("[").append(i).append(":a]volume=").append(individualVolumes[i]).append("[a").append(i).append("];");
                inputs.append("[a").append(i).append("]");
            }

            filterComplex.append(inputs).append("amix=inputs=").append(audioFiles.length).append(":duration=longest[v];[v]volume=").append(volume);

            List<String> commandList = new ArrayList<>();
            commandList.add(ffmpegPath);
            for(String audioFile : audioFiles) {
                commandList.add("-i");
                commandList.add(audioFile);
            }
            commandList.addAll(Arrays.asList(
                    "-filter_complex", filterComplex.toString(),
                    "-c:a", "pcm_s16le",
                    "-ar", "44100",
                    "-ac", "2",
                    outputFilePath,"-y"
            ));

            String[] command = commandList.toArray(new String[0]);

            String commandString = String.join(" ", command);
            System.out.println("Command: " + commandString);
            Instant startTime = Instant.now();
            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令输出信息
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Instant time1 = Instant.now();
                System.out.println("音频混音完成。"+"花费时间：" + Duration.between(startTime, time1).toMillis());
            } else {
                Instant time1 = Instant.now();
                System.out.println("音频混音失败。"+"花费时间：" + Duration.between(startTime, time1).toMillis());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 手动混音
     * @param output
     * @param soundTrack1
     * @param soundTrack2
     */
    public void soundMixer(File output,File soundTrack1,File soundTrack2)  {
        try {
            // Get the AudioInputStreams for the two audio files
            AudioInputStream stream1 = AudioSystem.getAudioInputStream(soundTrack1);
            AudioInputStream stream2 = AudioSystem.getAudioInputStream(soundTrack2);

            AudioFormat format = stream1.getFormat();

            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int read1, read2;
            while ((read1 = stream1.read(buffer1)) != -1 && (read2 = stream2.read(buffer2)) != -1) {
                byte[] mixed = new byte[read1];
                for (int i = 0; i < read1; i += 2) {
                    // read 16 bits from each stream and mix them
                    short audioSample1 = (short) (((buffer1[i + 1] & 0xff) << 8) | (buffer1[i] & 0xff));
                    short audioSample2 = (short) (((buffer2[i + 1] & 0xff) << 8) | (buffer2[i] & 0xff));
                    short mixedSample = (short) Math.min(Math.max(audioSample1 + audioSample2, Short.MIN_VALUE), Short.MAX_VALUE);
                    // write the mixed samples to the output array
                    mixed[i] = (byte) (mixedSample & 0xff);
                    mixed[i + 1] = (byte) ((mixedSample >> 8) & 0xff);
                }
                outputStream.write(mixed, 0, read1);
            }

            byte[] audioBytes = outputStream.toByteArray();
            InputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioBytes.length / format.getFrameSize());
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE,output);

            stream1.close();
            stream2.close();
            audioStream.close();
        }catch(IOException | UnsupportedAudioFileException e){
            e.printStackTrace();
        }
    }

    }
