// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.agent.runner.smart;

import com.alibaba.fastjson.JSONObject;
import com.microsoft.hydralab.common.entity.agent.SmartTestParam;
import com.microsoft.hydralab.common.util.Const;
import com.microsoft.hydralab.common.util.FileUtil;
import com.microsoft.hydralab.common.util.PythonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;

public class SmartTestUtil {
    private static String filePath = "";
    private static String folderPath = "";
    private static String stringFolderPath = "";
    Logger log = LoggerFactory.getLogger(SmartTestUtil.class);

    public SmartTestUtil(String location) {
        File testBaseDir = new File(location);
        String name = Const.SmartTestConfig.ZIP_FILE_NAME;
        String folderName = Const.SmartTestConfig.RESULT_FOLDER_NAME;

        folderPath = testBaseDir.getAbsolutePath() + "/" + Const.SmartTestConfig.ZIP_FOLDER_NAME + "/";
        stringFolderPath = testBaseDir.getAbsolutePath() + "/" + Const.SmartTestConfig.STRING_FOLDER_NAME
                + "/";

        File smartTestZip = new File(testBaseDir, name);
        File smartTestFolder = new File(testBaseDir, folderName);
        if (smartTestZip.exists()) {
            FileUtil.deleteFileRecursively(smartTestZip);
        }
        if (smartTestFolder.exists()) {
            FileUtil.deleteFileRecursively(smartTestFolder);
        }

        try (InputStream resourceAsStream = FileUtils.class.getClassLoader().getResourceAsStream(name);
             OutputStream out = new FileOutputStream(smartTestZip)) {
            if (resourceAsStream == null) {
                return;
            }
            IOUtils.copy(Objects.requireNonNull(resourceAsStream), out);
            FileUtil.unzipFile(smartTestZip.getAbsolutePath(), testBaseDir.getAbsolutePath());
            if (smartTestZip.exists()) {
                FileUtil.deleteFileRecursively(smartTestZip);
            }
        } catch (IOException e) {
            log.warn("SmartTestUtil init error", e);
        }
        initStringPool();
        filePath = folderPath + Const.SmartTestConfig.PY_FILE_NAME;
        File requireFile = new File(folderPath + Const.SmartTestConfig.REQUIRE_FILE_NAME);
        PythonUtil.installRequirements(requireFile, log);
    }

    public String runPYFunction(SmartTestParam smartTestParam, Logger logger) throws Exception {
        File smartTestFolder = new File(smartTestParam.getOutputFolder(), Const.SmartTestConfig.RESULT_FOLDER_NAME);
        Assert.isTrue(smartTestFolder.mkdir(), "create smartTestFolder failed");
        String res;
        String[] runArgs = new String[3];
        runArgs[0] = "python";
        runArgs[1] = filePath;
        runArgs[2] = writeConfigFile(smartTestParam).getAbsolutePath();

        for (String tempArg : runArgs) {
            logger.info(tempArg);
        }

        Process proc = Runtime.getRuntime().exec(runArgs);

        try (InputStream errorInput = proc.getErrorStream();
             InputStream inputStream = proc.getInputStream()) {
            SmartTestLog err = new SmartTestLog(errorInput, logger);
            SmartTestLog out = new SmartTestLog(inputStream, logger);
            err.start();
            out.start();
            proc.waitFor();
            res = out.getContent();
        } finally {
            proc.destroy();
        }

        return res;
    }

    // write config file
    public File writeConfigFile(SmartTestParam smartTestParam) {
        File configFile = new File(smartTestParam.getOutputFolder(), Const.SmartTestConfig.CONFIG_FILE_NAME);
        try {
            FileUtils.writeStringToFile(configFile, smartTestParam.toJSONString(), "UTF-8");
        } catch (IOException e) {
            log.warn("writeConfigFile error", e);
        }
        return configFile;
    }

    public JSONObject analysisRes(JSONObject data) {
        JSONObject coverage = data.getJSONObject(Const.SmartTestConfig.COVERAGE_TAG);
        JSONObject result = new JSONObject();
        Set<String> activityKeys = coverage.keySet();
        int totalActivity = activityKeys.size();
        int totalElement = 0;
        int visitedActivity = 0;
        int visitedElement = 0;

        for (String activityKey : activityKeys) {
            JSONObject activityInfo = coverage.getJSONObject(activityKey);
            if (!activityInfo.getBoolean(Const.SmartTestConfig.VISIT_TAG)) {
                continue;
            }
            Set<String> elementKeys = activityInfo.keySet();
            elementKeys.remove(Const.SmartTestConfig.VISIT_TAG);
            totalElement = totalElement + elementKeys.size();
            for (String elementKey : elementKeys) {
                if (activityInfo.getBoolean(elementKey)) {
                    visitedElement++;
                }
            }
            visitedActivity++;
        }
        result.put("activity", visitedActivity + "/" + totalActivity);
        result.put("element", visitedElement + "/" + visitedElement);
        return result;
    }

    public void initStringPool() {
        File stringDir = new File(stringFolderPath);
        if (!stringDir.exists()) {
            if (!stringDir.mkdirs()) {
                throw new RuntimeException("mkdirs fail for: " + stringDir);
            }
        }
        String[] fileNames = Const.SmartTestConfig.STRING_FILE_NAMES.split(",");
        for (String fileName : fileNames) {
            createTxtFile(stringFolderPath, fileName);
        }
    }

    public void createTxtFile(String path, String name) {
        String filenameTemp = path + name + ".txt";
        File filename = new File(filenameTemp);
        //generate string txt file if not exist
        if (!filename.exists()) {
            try {
                Assert.isTrue(filename.createNewFile(), "createTxtFile error " + filename.getAbsolutePath());
            } catch (IOException e) {
                log.warn("createTxtFile error " + filename.getAbsolutePath(), e);
            }
        }
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getStringFolderPath() {
        return stringFolderPath;
    }
}
