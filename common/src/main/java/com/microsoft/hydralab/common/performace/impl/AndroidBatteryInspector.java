// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.hydralab.common.performace.impl;

import com.microsoft.hydralab.performance.PerformanceInspectionResult;
import com.microsoft.hydralab.performance.PerformanceInspector;
import com.microsoft.hydralab.performance.PerformanceResult;
import com.microsoft.hydralab.performance.PerformanceTestSpec;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class AndroidBatteryInspector implements PerformanceInspector {

    @Override
    public void initialize(PerformanceTestSpec performanceTestSpec, File resultFolder) {

    }

    @Override
    public PerformanceInspectionResult inspect(PerformanceTestSpec performanceTestSpec, File resultFolder) {
        if ((performanceTestSpec.getTypeFlag() & PerformanceTestSpec.FLAG_BATTERY) == 0
                || !"android".equals(performanceTestSpec.getDeviceId().toLowerCase(Locale.ROOT))) return null;
        // else capture performance metrics
        return null;
    }

    @Override
    public PerformanceResult<?> parse(List<PerformanceInspectionResult> performanceInspectionResultList) {
        return null;
    }
}
