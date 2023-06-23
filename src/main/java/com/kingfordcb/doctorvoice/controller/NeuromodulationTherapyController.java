package com.kingfordcb.doctorvoice.controller;

import com.kingfordcb.doctorvoice.entity.LeftVoice;
import com.kingfordcb.doctorvoice.service.NeuromodulationTherapyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 神经调控治疗
 * @Author: lpf
 * @Date: 2023/06/07
 */
@RestController
@RequestMapping("/neuromodulationtherapy")
public class NeuromodulationTherapyController {

    @Autowired
    private NeuromodulationTherapyService neuromodulationTherapyService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        String filePath =  filename;

        // 创建文件资源
        Resource resource = new FileSystemResource(filePath);

        // 检查文件是否存在
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);

        // 返回文件作为响应
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    /*@PostMapping(value = "/getLeftAudio")
    public Result getLeftAudio(@RequestBody LeftVoice leftVoice) throws FileNotFoundException {
        File leftVoiceMP3 = neuromodulationTherapyService.leftear(leftVoice);
        String mp3FilePath ="/neuromodulationtherapy/"+leftVoiceMP3.getPath();
        return Result.success(mp3FilePath);
    }*/
    @PostMapping(value = "/getLeftAudio", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getLeftAudio(@RequestBody LeftVoice leftVoice) throws FileNotFoundException {
        String leftVoicePath = neuromodulationTherapyService.leftear(leftVoice);
        Path path = Paths.get(leftVoicePath);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("Filename:"+resource.getFilename()+" resource"+resource);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

}
