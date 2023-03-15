// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.hydralab.common.file.impl.azure;

import com.microsoft.hydralab.common.file.StorageProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhoule
 * @date 11/15/2022
 */

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage.azure")
@Component
public class AzureBlobProperty extends StorageProperties {
    private String connection;
    private long sasExpiryTimeFront;
    private long sasExpiryTimeAgent;
    private long sasExpiryUpdate;
    private String timeUnit;
    private int fileLimitDay;
    private String cdnUrl;
}
