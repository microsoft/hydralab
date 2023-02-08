// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.hydralab

import com.microsoft.hydralab.config.DeviceConfig
import com.microsoft.hydralab.config.HydraLabAPIConfig
import com.microsoft.hydralab.config.TestConfig
import com.microsoft.hydralab.utils.HydraLabClientUtils
import org.apache.commons.lang3.StringUtils
import org.gradle.api.Plugin
import org.gradle.api.Project


class ClientUtilsPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.task("requestHydraLabTest") {
            doFirst {
                TestConfig testConfig = new TestConfig()
                DeviceConfig deviceConfig = new DeviceConfig()
                HydraLabAPIConfig apiConfig = new HydraLabAPIConfig()

                if (project.hasProperty('appPath')) {
                    def appFile = project.file(project.appPath)
                    println("Param appPath: ${project.appPath}")
                    if (!appFile.exists()) {
                        def exceptionMsg = "${project.appPath} file not exist!"
                        throw new Exception(exceptionMsg)
                    } else {
                        testConfig.appPath = appFile.absolutePath
                    }
                }

                if (project.hasProperty('testAppPath')) {
                    def testAppFile = project.file(project.testAppPath)
                    println("Param testAppPath: ${project.testAppPath}")
                    if (!testAppFile.exists()) {
                        def exceptionMsg = "${project.testAppPath} file not exist!"
                        throw new Exception(exceptionMsg)
                    } else {
                        testConfig.testAppPath = testAppFile.absolutePath
                    }
                }

                if (project.hasProperty('attachmentConfigPath')) {
                    def attachmentConfigFile = project.file(project.attachmentConfigPath)
                    println("Param attachmentConfigPath: ${project.attachmentConfigPath}")
                    if (!attachmentConfigFile.exists()) {
                        def exceptionMsg = "${project.attachmentConfigPath} file not exist!"
                        throw new Exception(exceptionMsg)
                    } else {
                        testConfig.attachmentConfigPath = attachmentConfigFile.absolutePath
                    }
                }

                def reportDir = new File(project.buildDir, "testResult")
                if (!reportDir.exists()) reportDir.mkdirs()

                def argsMap = null
                if (project.hasProperty('instrumentationArgs')) {
                    argsMap = [:]
                    // quotation marks not support
                    def argLines = project.instrumentationArgs.replace("\"", "").split(",")
                    for (i in 0..<argLines.size()) {
                        String[] kv = argLines[i].split("=")
                        // use | to represent comma to avoid conflicts
                        argsMap.put(kv[0], kv[1].replace("|", ","))
                    }
                }

                def extraArgsMap = null
                if (project.hasProperty('extraArgs')) {
                    extraArgsMap = [:]
                    // quotation marks not support
                    def argLines = project.extraArgs.replace("\"", "").split(",")
                    for (i in 0..<argLines.size()) {
                        String[] kv = argLines[i].split("=")
                        // use | to represent comma to avoid conflicts
                        extraArgsMap.put(kv[0], kv[1].replace("|", ","))
                    }
                }

                if (project.hasProperty('hydraLabAPISchema')) {
                    apiConfig.schema = project.hydraLabAPISchema
                }
                if (project.hasProperty('hydraLabAPIHost')) {
                    apiConfig.host = project.hydraLabAPIHost
                }
                if (project.hasProperty('authToken')) {
                    apiConfig.authToken = project.authToken
                }
                if (project.hasProperty('onlyAuthPost')) {
                    apiConfig.onlyAuthPost = Boolean.parseBoolean(project.onlyAuthPost)
                }

                if (project.hasProperty('deviceIdentifier')) {
                    deviceConfig.deviceIdentifier = project.deviceIdentifier
                }
                if (project.hasProperty('groupTestType')) {
                    deviceConfig.groupTestType = project.groupTestType
                }
                if (project.hasProperty('neededPermissions')) {
                    deviceConfig.neededPermissions = project.neededPermissions.split(", +")
                }
                if (project.hasProperty('deviceActions')) {
                    // add quotes back as quotes in gradle plugins will be replaced by blanks
                    deviceConfig.deviceActionsStr = project.deviceActions.replace("\\", "\"")
                }

                if (project.hasProperty('runningType')) {
                    testConfig.runningType = project.runningType
                }
                if (project.hasProperty('pkgName')) {
                    testConfig.pkgName = project.pkgName
                }
                if (project.hasProperty('testPkgName')) {
                    testConfig.testPkgName = project.testPkgName
                }
                if (project.hasProperty('teamName')) {
                    testConfig.teamName = project.teamName
                }
                if (project.hasProperty('testRunnerName')) {
                    testConfig.testRunnerName = project.testRunnerName
                }
                if (project.hasProperty('testScope')) {
                    testConfig.testScope = project.testScope
                }
                if (project.hasProperty('testSuiteName')) {
                    testConfig.testSuiteName = project.testSuiteName
                }
                if (project.hasProperty('frameworkType')) {
                    testConfig.frameworkType = project.frameworkType
                }
                if (project.hasProperty('runTimeOutSeconds')) {
                    testConfig.runTimeOutSeconds = Integer.parseInt(project.runTimeOutSeconds)
                }
                testConfig.queueTimeOutSeconds = testConfig.runTimeOutSeconds
                if (project.hasProperty('queueTimeOutSeconds')) {
                    testConfig.queueTimeOutSeconds = Integer.parseInt(project.queueTimeOutSeconds)
                }
                if (project.hasProperty('maxStepCount')) {
                    testConfig.maxStepCount = Integer.parseInt(project.maxStepCount)
                }
                if (project.hasProperty('deviceTestCount')) {
                    testConfig.deviceTestCount = Integer.parseInt(project.deviceTestCount)
                }
                if (project.hasProperty('needUninstall')) {
                    testConfig.needUninstall = Boolean.parseBoolean(project.needUninstall)
                }
                if (project.hasProperty('needClearData')) {
                    testConfig.needClearData = Boolean.parseBoolean(project.needClearData)
                }
                if (project.hasProperty('tag')) {
                    testConfig.artifactTag = project.tag
                }

                requiredParamCheck(apiConfig, deviceConfig, testConfig)

                HydraLabClientUtils.runTestOnDeviceWithApp(
                        reportDir.absolutePath, argsMap, extraArgsMap,
                        apiConfig, deviceConfig, testConfig
                )
            }
        }.configure {
            group = "Test"
            description = "Run mobile/cross-platform test with specified params on Hydra Lab - see more in https://github.com/microsoft/HydraLab/wiki"
        }
    }

    void requiredParamCheck(HydraLabAPIConfig apiConfig, DeviceConfig deviceConfig, TestConfig testConfig) {
        if (StringUtils.isBlank(apiConfig.authToken)
                || StringUtils.isBlank(testConfig.appPath)
                || StringUtils.isBlank(testConfig.pkgName)
                || StringUtils.isBlank(testConfig.runningType)
                || testConfig.runTimeOutSeconds == -1
                || StringUtils.isBlank(deviceConfig.deviceIdentifier)
        ) {
            throw new IllegalArgumentException('Required params not provided! Make sure the following params are all provided correctly: authToken, appPath, pkgName, runningType, runTimeOutSeconds, deviceIdentifier.')
        }

        // running type specified params
        switch (testConfig.runningType) {
            case "INSTRUMENTATION":
                if (StringUtils.isBlank(testConfig.testAppPath)) {
                    throw new IllegalArgumentException('Required param testAppPath not provided!')
                }
                if (StringUtils.isBlank(testConfig.testPkgName)) {
                    throw new IllegalArgumentException('Required param testPkgName not provided!')
                }
                if (testConfig.testScope != TestScope.PACKAGE && testConfig.testScope != TestScope.CLASS) {
                    break
                }
                if (StringUtils.isBlank(testConfig.testSuiteName)) {
                    throw new IllegalArgumentException('Required param testSuiteName not provided!')
                }
                break
            case "APPIUM":
                if (StringUtils.isBlank(testConfig.testAppPath)) {
                    throw new IllegalArgumentException('Required param testAppPath not provided!')
                }
                if (StringUtils.isBlank(testConfig.testSuiteName)) {
                    throw new IllegalArgumentException('Required param testSuiteName not provided!')
                }
                break
            case "APPIUM_CROSS":
                if (StringUtils.isBlank(testConfig.testAppPath)) {
                    throw new IllegalArgumentException('Required param testAppPath not provided!')
                }
                if (StringUtils.isBlank(testConfig.testSuiteName)) {
                    throw new IllegalArgumentException('Required param testSuiteName not provided!')
                }
                break
            case "SMART":
                break
            case "T2C_JSON":
                break
            case "APPIUM_MONKEY":
                break
            case "MONKEY":
                break
            default:
                break
        }
    }

    interface TestScope {
        String TEST_APP = "TEST_APP";
        String PACKAGE = "PACKAGE";
        String CLASS = "CLASS";
    }
}
