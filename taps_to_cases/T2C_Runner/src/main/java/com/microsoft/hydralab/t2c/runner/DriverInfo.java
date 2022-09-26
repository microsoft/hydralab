// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.hydralab.t2c.runner;

public class DriverInfo {
    private String id;
    private String platform;
    private String launcherApp;
    private String initURL;

    public DriverInfo(String id, String platform, String launcherApp, String initURL){
        this.id = id;
        this.platform = platform;
        this.launcherApp = launcherApp;
        this.initURL = initURL;
    }

    public String getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getInitURL() {
        return initURL;
    }

    public String getLauncherApp() {
        return launcherApp;
    }
}
