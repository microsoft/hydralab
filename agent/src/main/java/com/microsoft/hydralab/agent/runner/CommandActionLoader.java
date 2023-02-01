// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.hydralab.agent.runner;

import com.microsoft.hydralab.common.entity.agent.DeviceCommand;
import com.microsoft.hydralab.common.entity.common.DeviceAction;
import com.microsoft.hydralab.common.entity.common.TestTask;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhoule
 * @date 01/31/2023
 */
@Service
public class CommandActionLoader {
    @Resource(name = "DeviceCommandProperty")
    private List<DeviceCommand> commands;

    public void loadCommandAction(TestTask testTask) {
        if (testTask.getDeviceActions() == null) {
            testTask.setDeviceActions(new HashMap<>());
        }
        List<DeviceCommand> filteredCommands = filterCommands(testTask.getTestSuite());
        for (DeviceCommand deviceCommand : filteredCommands) {
            List<DeviceAction> actions = command2Action(deviceCommand);
            List<DeviceAction> originActions = testTask.getDeviceActions().getOrDefault(deviceCommand.getWhen(), new ArrayList<>());
            originActions.addAll(actions);
            testTask.getDeviceActions().put(deviceCommand.getWhen(), originActions);
        }
    }

    private List<DeviceCommand> filterCommands(String suiteName) {
        List<DeviceCommand> filteredCommands = new ArrayList<>();
        for (DeviceCommand command : commands) {
            if (suiteName.matches(command.getMatcher())) {
                filteredCommands.add(command);
            }
        }
        return filteredCommands;
    }

    private List<DeviceAction> command2Action(DeviceCommand deviceCommand) {
        List<DeviceAction> actionList = new ArrayList<>();
        ActionConverter converter = ActionConverter.valueOf(deviceCommand.getType());
        if (converter == null) {
            return actionList;
        }
        String[] commandLines = deviceCommand.getInline().split("\n");
        for (String commandLine : commandLines) {
            if (!StringUtils.isEmpty(commandLine)) {
                actionList.add(converter.getAction(commandLine));
            }
        }
        return actionList;
    }

    private enum ActionConverter {
        //generate action by command type
        ADBShell() {
            @Override
            public DeviceAction getAction(String commandline) {
                DeviceAction deviceAction = new DeviceAction("Android", "execCommandOnDevice");
                deviceAction.getArgs().add(commandline);
                return deviceAction;
            }
        };

        public abstract DeviceAction getAction(String commandline);
    }
}
