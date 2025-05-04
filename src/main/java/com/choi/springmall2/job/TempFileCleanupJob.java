package com.choi.springmall2.job;

import com.choi.springmall2.service.RedisFileKeyService;
import com.choi.springmall2.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupJob implements Job {

    private final S3Service s3Service;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("🧹 [Quartz] Temp file cleanup job started...");
        List<String> deletedKeys = s3Service.deleteTempFolderFiles();
        log.info("🧹 [Quartz] Temp file cleanup job completed. Deleted keys: {}", deletedKeys);
    }
}
