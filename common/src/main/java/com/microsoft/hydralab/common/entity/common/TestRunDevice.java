// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.common.entity.common;

import cn.hutool.core.img.gif.AnimatedGifEncoder;
import com.microsoft.hydralab.common.logger.LogCollector;
import com.microsoft.hydralab.common.screen.ScreenRecorder;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

import java.io.File;

@Getter
@Setter
public class TestRunDevice{
    private final DeviceInfo deviceInfo;
    private final String tag;
    private transient ScreenRecorder screenRecorder;
    private transient LogCollector logCollector;
    private String logPath;
    private final transient AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
    private int gifFrameCount = 0;
    private transient File gifFile;

    private transient WebDriver webDriver;

    public TestRunDevice(DeviceInfo deviceInfo, String tag) {
        this.deviceInfo = deviceInfo;
        this.tag = tag;
    }
}