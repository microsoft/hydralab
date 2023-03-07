import Select, { MultiValue } from 'react-select'
import React, { PureComponent } from 'react'
import axios from "@/axios";
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, Legend } from 'recharts';

const COLORS = ['#007FFF', '#FFA500', '#8B8970', '#800000', '#FFCC00', '#808000', '#4B0080', '#8884d8', '#7EC0EE',
    '#8B008B', '#EE7600', '#CD5C5C', '#BC8F8F', '#8B8B7A', '#006400', '#FF69B4', '#90EE90', '#A4D3EE',
    '#8884d8', '#8884d8', '#8884d8', '#8884d8', '#8884d8', '#8884d8', '#8884d8', '#8884d8', '#8884d8'];
const batteryKey = ['appUsage', 'cpu', 'screen', 'wakeLock', 'systemService', 'wifi', 'total'];
const memoryKey = ['javaHeapPss', 'nativeHeapRss', 'nativeHeapPss', 'codePss', 'codeRss', 'stackPss', 'stackRss',
    'graphicsPss', 'graphicsRss', 'privateOtherPss', 'privateOtherRss', 'systemPss', 'systemRss',
    'unknownPss', 'unknownRss', 'totalPss', 'totalRss', 'totalSwapPss'];

export default class PerfTestDashboard extends React.Component {
    state = {
        task: this.props.testTask,
        memoryInfo: undefined,
        batteryInfo: undefined,
    };

    render() {
        const task = this.state.task;
        const memoryInfo = this.state.memoryInfo;
        const memoryMetrics = [];
        const batteryInfo = this.state.batteryInfo;
        const batteryMetrics = [];

        /**
         * Battery Info
         */
        if (batteryInfo && batteryInfo.performanceInspectionResults && batteryInfo.performanceInspectionResults.length > 0) {
            let startTime = batteryInfo.performanceInspectionResults[0].timestamp;
            batteryInfo.performanceInspectionResults.forEach((inspectionResult) => {
                inspectionResult.parsedData.time = (inspectionResult.timestamp - startTime) / 1000;
                inspectionResult.parsedData.ratio = inspectionResult.parsedData.ratio * 100;
                batteryMetrics.push(inspectionResult.parsedData);
            })
        }

        const renderBatteryChart = (
            <LineChart width={800} height={400} data={batteryMetrics} margin={{ top: 20, right: 100, bottom: 20, left: 20 }}>
                <XAxis dataKey="time" label={{ value: 'Time', position: 'bottom' }} unit="s" />
                <YAxis yAxisId="left" label={{ value: 'Battery usage (mAh)', angle: -90, position: 'left' }} />
                <YAxis yAxisId="right" label={{ value: 'Ratio', angle: -90, position: 'right' }} unit="%" orientation="right" />
                {batteryKey.map((key, index) => (
                    <Line type="monotone" yAxisId="left" dataKey={key} stroke={COLORS[index]} />
                ))}
                <Line type="monotone" yAxisId="right" dataKey="ratio" stroke='#8833d8' />
                {/* <CartesianGrid stroke="#ccc" strokeDasharray="5 5" /> */}
                <Tooltip />
                <Legend verticalAlign="top" />
            </LineChart>)


        /**
         * Memory Info
         */
        if (memoryInfo && memoryInfo.performanceInspectionResults && memoryInfo.performanceInspectionResults.length > 0) {
            let startTime = memoryInfo.performanceInspectionResults[0].timestamp;
            memoryInfo.performanceInspectionResults.forEach((inspectionResult) => {
                inspectionResult.parsedData.time = (inspectionResult.timestamp - startTime) / 1000;
                Object.keys(inspectionResult.parsedData).forEach((key) => {
                    if (inspectionResult.parsedData[key] == -1) {
                        inspectionResult.parsedData[key] = 0;
                    }
                })
                memoryMetrics.push(inspectionResult.parsedData);
            })
        }

        const renderMemoryChart = (
            <LineChart width={800} height={400} data={memoryMetrics} margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                <Legend verticalAlign="top" />
                <XAxis dataKey="time" label={{ value: 'Time', position: 'bottom' }} unit="s" />
                <YAxis yAxisId="left" label={{ value: 'Memory usage (KB)', angle: -90, position: 'left' }} />
                {memoryKey.map((key, index) => (
                    <Line type="monotone" yAxisId="left" dataKey={key} stroke={COLORS[index]} />
                ))}
                {/* <CartesianGrid stroke="#ccc" strokeDasharray="5 5" /> */}
                <Tooltip />

            </LineChart>)

        return <div id='perf_dashboard'>
            {batteryInfo && <h3> Battery report</h3>}
            {batteryInfo && renderBatteryChart}
            {memoryInfo && <h3> Memory report</h3>}
            {memoryInfo && renderMemoryChart}
        </div>
    };

    getPerfReportJson() {
        axios.get('https://devicelabstorage.blob.core.windows.net/testresults/test/result/PerformanceReportCopy.json' + '?' + require('local-storage').get('BlobSignature'), {
        }).then(res => {
            console.log(res.data);
            for (var info of res.data) {
                console.log(info);
                if (info.parserType == 'PARSER_ANDROID_BATTERY_INFO') {
                    this.setState({ batteryInfo: info });
                } else if (info.parserType == 'PARSER_ANDROID_MEMORY_INFO') {
                    this.setState({ memoryInfo: info });
                }
            };
        })
    }

    componentDidMount() {
        this.getPerfReportJson();
    }
}