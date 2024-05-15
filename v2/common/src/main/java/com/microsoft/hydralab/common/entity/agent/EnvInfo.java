// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.common.entity.agent;

import lombok.Data;

import java.util.List;

@Data
public class EnvInfo {
    private String osName;
    private String osVersion;
    private String userName;
    private OS os;
    private List<EnvCapability> capabilities;

    public void setCapabilities(List<EnvCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public List<EnvCapability> getCapabilities() {
        return capabilities;
    }

    public enum OS {
        WINDOWS,
        LINUX,
        MACOS,
        UNKNOWN
    }
}
