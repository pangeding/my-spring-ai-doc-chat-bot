package com.pangeding.springaidocchatbot.contoller;

import com.pangeding.springaidocchatbot.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class DocController {

    @Autowired
    private DocService docService;

    // 上传文档接口
    @PostMapping("/ai/doc/upload")
    public String uploadDoc(@RequestParam("file") MultipartFile file) {
        // 1. 记录上传请求基本信息
        log.info("收到文件上传请求，文件名: [{}]，原始文件名: [{}]，文件大小: [{}KB]，文件类型: [{}]",
                file.getName(),
                file.getOriginalFilename(),
                file.getSize() / 1024.0,  // 转换为KB
                file.getContentType());

        // 2. 校验文件是否为空
        if (file.isEmpty()) {
            log.warn("上传文件为空，文件名: [{}]", file.getOriginalFilename());
            return "上传失败：文件内容为空";
        }

        try {
            // 3. 记录开始处理
            log.info("开始处理文件 [{}]，准备调用文档处理服务", file.getOriginalFilename());

            // 4. 调用服务处理文件
            docService.processAndStoreDocument(file);

            // 5. 记录处理成功
            log.info("文件 [{}] 处理完成并成功存储，大小: [{}KB]",
                    file.getOriginalFilename(),
                    file.getSize() / 1024.0);
            return "文档上传并处理成功";
        } catch (Exception e) {
            // 6. 记录异常详情（包含堆栈跟踪）
            log.error("文件 [{}] 处理失败，错误信息: [{}]",
                    file.getOriginalFilename(),
                    e.getMessage(),
                    e);  // 第三个参数传入异常对象，打印完整堆栈
            return "处理失败：" + e.getMessage();
        }
    }
}

