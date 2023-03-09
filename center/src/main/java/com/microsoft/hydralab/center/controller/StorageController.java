// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.center.controller;

import com.microsoft.hydralab.center.service.StorageTokenManageService;
import com.microsoft.hydralab.common.entity.agent.Result;
import com.microsoft.hydralab.common.entity.center.SysUser;
import com.microsoft.hydralab.common.util.Const;
import com.microsoft.hydralab.common.util.HydraLabRuntimeException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Li Shen
 * @date 2/20/2023
 */

@RestController
@RequestMapping
public class StorageController {
    private final Logger logger = LoggerFactory.getLogger(StorageController.class);

    @Resource
    private StorageTokenManageService storageTokenManageService;

    @PostMapping(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_UPLOAD)
    public Result uploadFile(HttpServletRequest request,
                             @RequestParam("file") MultipartFile uploadedFile,
                             @RequestParam("container") String container,
                             @RequestParam("fileRelPath") String fileRelPath) {
        String storageToken = request.getHeader("Authorization");
        if (storageToken != null) {
            storageToken = storageToken.replaceAll("Bearer ", "");
        } else {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
        }
        if (!storageTokenManageService.validateToken(storageToken)) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
        }

        String fileUri = Const.LocalStorageURL.CENTER_LOCAL_STORAGE_DIR + container + "/" + fileRelPath;
        File file = new File(fileUri);
        File parentDirFile = new File(file.getParent());
        if (!parentDirFile.exists() && !parentDirFile.mkdirs()) {
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "mkdirs failed!");
        }

        InputStream inputStream;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            inputStream = uploadedFile.getInputStream();
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "upload file failed!");
        }
        return Result.ok(fileUri);
    }

    @PostMapping(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_DOWNLOAD)
    public Result<FileSystemResource> downloadFile(HttpServletRequest request,
                                                   @RequestParam("container") String container,
                                                   @RequestParam("fileRelPath") String fileRelPath) {
        String storageToken = request.getHeader("Authorization");
        if (storageToken != null) {
            storageToken = storageToken.replaceAll("Bearer ", "");
        } else {
//            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
        }
        if (!storageTokenManageService.validateToken(storageToken)) {
//            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
        }

        String fileUri = Const.LocalStorageURL.CENTER_LOCAL_STORAGE_DIR + container + "/" + fileRelPath;
        File file = new File(fileUri);
        if (!file.exists()) {
//            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
            throw new HydraLabRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.value(), String.format("File %s not exist!", fileUri));
        }

        return Result.ok(new FileSystemResource(file));
    }

//    // todo: for front end to download file
//    @GetMapping("/api/storage/local/download/{container}/**")
//    public Result downloadFile(HttpServletRequest request,
//                               @PathVariable("container") String container,
////                               // todo: 无需传fileToken = 格式should be like:
////                               //  sv=2020-10-02&ss=b&srt=o&se=2023-03-07T09%3A50%3A05Z&sp=r&sig=Uzb8EBKAdk6DTlqHFtsgqdPsXRltdilDujzvOQKpzAQ%3D
////                               @QueryParam("expiryTime") String expiryTime,
////                               @QueryParam("permission") String permission,
////                               @QueryParam("signature") String signature,
//                               @QueryParam("fileToken") String fileToken) {
//        // todo: .apk/.ipa/.txt 后缀匹配
//        if (!storageTokenManageService.validateToken(fileToken)) {
//            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
//        }
//        // todo: container match existing ones
////        if (!contains(container)){
////            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
////        }
//
//        final String appendPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
//        final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
//        String fileRelPath = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, appendPath);
////        if (StringUtils.isEmpty(fileRelPath)) {
////            moduleName = container + '/' + fileRelPath;
////        } else {
////            moduleName = container;
////        }
//
//        if (fileToken == null) {
//            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
//        }
//
//
//        // todo: test output
//        try (FileInputStream in = new FileInputStream(file);
//        ServletOutputStream out = response.getOutputStream()) {
//            response.setContentType("application/octet-stream");
//            response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
//            int len;
//            byte[] buffer = new byte[1024 * 10];
//            while ((len = in.read(buffer)) != -1) {
//                out.write(buffer, 0, len);
//            }
//            out.flush();
//
//        } finally {
//            response.flushBuffer();
//        }
//
//        // todo:
//        return Result.ok(fileToken);
//    }

    @GetMapping("/api/storage/getToken")
    public Result generateReadToken(@CurrentSecurityContext SysUser requestor) {
        if (requestor == null) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "unauthorized");
        }
        return Result.ok(storageTokenManageService.generateReadToken(requestor.getMailAddress()).getToken());
    }
}
