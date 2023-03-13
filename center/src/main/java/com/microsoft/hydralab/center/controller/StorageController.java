// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.center.controller;

import com.azure.core.annotation.QueryParam;
import com.microsoft.hydralab.center.service.StorageTokenManageService;
import com.microsoft.hydralab.common.entity.agent.Result;
import com.microsoft.hydralab.common.entity.center.SysUser;
import com.microsoft.hydralab.common.util.Const;
import com.microsoft.hydralab.common.util.HydraLabRuntimeException;
import com.microsoft.hydralab.common.util.LogUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Li Shen
 * @date 2/20/2023
 */

@RestController
@RequestMapping
public class StorageController {
    private final Logger logger = LoggerFactory.getLogger(StorageController.class);

    @javax.annotation.Resource
    private StorageTokenManageService storageTokenManageService;

    @PostMapping(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_UPLOAD)
    public Result uploadFile(HttpServletRequest request,
                             @RequestParam("file") MultipartFile uploadedFile,
                             @RequestParam("fileUri") String fileUri) {
        String storageToken = request.getHeader("Authorization");
        if (storageToken != null) {
            storageToken = storageToken.replaceAll("Bearer ", "");
        } else {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
        }
        if (!storageTokenManageService.validateAccessToken(storageToken)) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
        }
        if (!LogUtils.isLegalStr(fileUri, Const.RegexString.STORAGE_FILE_REL_PATH, false)) {
            throw new HydraLabRuntimeException(HttpStatus.BAD_REQUEST.value(), "Invalid file path!");
        }
        File file = new File(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_ROOT + fileUri);
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

    // used by center/agent
    @PostMapping(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_DOWNLOAD)
    public void postDownloadFile(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("fileUri") String fileUri) {
        String storageToken = request.getHeader("Authorization");
        if (storageToken != null) {
            storageToken = storageToken.replaceAll("Bearer ", "");
        } else {
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
        }
        if (!storageTokenManageService.validateAccessToken(storageToken)) {
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
        }
        if (!LogUtils.isLegalStr(fileUri, Const.RegexString.STORAGE_FILE_REL_PATH, false)) {
            throw new HydraLabRuntimeException(HttpStatus.BAD_REQUEST.value(), "Invalid file path!");
        }

        copyDownloadStreamToResponse(fileUri, response);
    }

    // for front end to download file
    @GetMapping("/api/storage/local/download/**")
    public void getDownloadFile(HttpServletRequest request,
                                HttpServletResponse response,
                                @QueryParam("token") String token) {
        if (token == null) {
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Invalid visit with no auth code");
        }
        if (!storageTokenManageService.validateTokenVal(token)) {
            throw new HydraLabRuntimeException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized, error access token for storage actions.");
        }
        final String appendPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String fileUri = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, appendPath);
        if (!LogUtils.isLegalStr(fileUri, Const.RegexString.STORAGE_FILE_REL_PATH, false)) {
            throw new HydraLabRuntimeException(HttpStatus.BAD_REQUEST.value(), "Invalid file path!");
        }

        copyDownloadStreamToResponse(fileUri, response);
    }

    @GetMapping("/api/storage/getToken")
    public Result generateReadToken(@CurrentSecurityContext SysUser requestor) {
        if (requestor == null) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "unauthorized");
        }
        return Result.ok(storageTokenManageService.generateReadToken(requestor.getMailAddress()).getToken());
    }

    private void copyDownloadStreamToResponse(String fileUri, HttpServletResponse response) {
        File file = new File(Const.LocalStorageURL.CENTER_LOCAL_STORAGE_ROOT + fileUri);
        if (!file.exists()) {
            throw new HydraLabRuntimeException(HttpStatus.BAD_REQUEST.value(), String.format("File %s not exist!", fileUri));
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            OutputStream os = response.getOutputStream();
            int resLen = IOUtils.copy(bis, os);
            logger.info(String.format("Output file: %s , size: %d!", fileUri, resLen));
        } catch (IOException e) {
            throw new HydraLabRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
