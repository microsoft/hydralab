// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.hydralab.performance;

public interface PerformanceInspector {

    enum PerformanceInspectorType {
        INSPECTOR_ANDROID_MEMORY_DUMP("INSPECTOR_ANDROID_MEMORY_DUMP"),
        INSPECTOR_ANDROID_MEMORY_INFO("INSPECTOR_ANDROID_MEMORY_INFO"),
        INSPECTOR_ANDROID_BATTERY_INFO("INSPECTOR_ANDROID_BATTERY_INFO"),
        INSPECTOR_WIN_MEMORY("INSPECTOR_WIN_MEMORY"),
        INSPECTOR_WIN_BATTERY("INSPECTOR_WIN_BATTERY");

        private final String name;

        PerformanceInspectorType(final String typeName) {
            this.name = typeName;
        }

        public String getName() {
            return name;
        }
    }

    PerformanceInspectionResult inspect(PerformanceInspection performanceInspection);

}
