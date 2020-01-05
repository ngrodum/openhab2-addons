package org.openhab.binding.adaxheater.cloudapi;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdaxJsonMarshaller {
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(Include.NON_NULL);
            this.objectMapper = objectMapper;
        }
        return this.objectMapper;
    }

    public <T> String writeSpecificItem(T item, Class<T> aClass) throws JsonProcessingException {
        if (item == null) {
            return null;
        }
        if (aClass == null) {
            return null;
        }
        if (aClass.equals(Long.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
        if (aClass.equals(Integer.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
        if (aClass.equals(Boolean.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
        if (aClass.equals(UserLoginData.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
//        if (aClass.equals(HeaterLoginData.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(ServiceVersionInfo.class)) {
//            return getObjectMapper().writeValueAsString(item);
        //      }
        if (aClass.equals(ClientErrorHolder.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
        if (aClass.equals(HeaterInfo.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
        if (aClass.equals(Zone.class)) {
            return getObjectMapper().writeValueAsString(item);
        }
//        if (aClass.equals(Schedule.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(HeatingMode.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(ScheduleInterval.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(HeaterInfoForDevice.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(DeviceDescriptor.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(HeaterScheduleIntervals.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(HeaterScheduleIntervalsBinary.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(Firmware.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
//        if (aClass.equals(TemperaturesLog.class)) {
//            return getObjectMapper().writeValueAsString(item);
//        }
        return getObjectMapper().writeValueAsString(item);
    }

    public <T> T parseSpecificItem(JsonNode node, Class<T> aClass, ObjectMapper objectMapper) throws JsonProcessingException {
        if (node == null) {
            return null;
        }
        if (aClass == null) {
            return null;
        }
        if (aClass.equals(Long.class)) {
            return (T) getNodeValueLong(node);
        }
        if (aClass.equals(Integer.class)) {
            return (T) getNodeValueInt(node);
        }
        if (aClass.equals(Boolean.class)) {
            return (T) getNodeValueBool(node);
        }
        if (aClass.equals(UserLoginData.class)) {
            return (T) readUserLoginData(node);
        }
//        if (aClass.equals(HeaterLoginData.class)) {
//            return (T)readHeaterLoginData(node, dataModelVersion);
//        }
//        if (aClass.equals(ServiceVersionInfo.class)) {
//            return readServiceVersionInfo(node, dataModelVersion);
//        }
        if (aClass.equals(ClientErrorHolder.class)) {
            return (T) readClientErrorHolder(node);
        }
        if (aClass.equals(HeaterInfo.class)) {
            return (T) readHeaterInfo(node);
        }
        if (aClass.equals(Zone.class)) {
            return (T) readZone(node);
        }
//        if (aClass.equals(Schedule.class)) {
//            return readSchedule(node, dataModelVersion);
//        }
//        if (aClass.equals(HeatingMode.class)) {
//            return readHeatingMode(node, dataModelVersion);
//        }
//        if (aClass.equals(ScheduleInterval.class)) {
//            return readScheduleInterval(node, dataModelVersion);
//        }
//        if (aClass.equals(HeaterInfoForDevice.class)) {
//            return readHeaterInfoForDevice(node, dataModelVersion);
//        }
//        if (aClass.equals(DeviceDescriptor.class)) {
//            return readDeviceDescriptor(node, dataModelVersion);
//        }
//        if (aClass.equals(HeaterScheduleIntervals.class)) {
//            return readHeaterScheduleIntervals(node, dataModelVersion);
//        }
//        if (aClass.equals(HeaterScheduleIntervalsBinary.class)) {
//            return readHeaterScheduleIntervalsBinary(node, dataModelVersion);
//        }
//        if (aClass.equals(Firmware.class)) {
//            return readFirmware(node, dataModelVersion, objectMapper);
//        }
//        if (aClass.equals(TemperaturesLog.class)) {
//            return readTemperaturesLog(node, dataModelVersion, objectMapper);
//        }
        return objectMapper.treeToValue(node, aClass);
    }

    private static String getNodeValueString(JsonNode node) {
        if (node == null || node.getNodeType() != JsonNodeType.STRING) {
            return null;
        }
        return node.textValue();
    }

    private static Long getNodeValueLong(JsonNode node) {
        if (node == null || node.getNodeType() != JsonNodeType.NUMBER) {
            return null;
        }
        Number num = node.numberValue();
        if (num instanceof Long) {
            return (Long) num;
        }
        return Long.valueOf(num.longValue());
    }

    private static Integer getNodeValueInt(JsonNode node) {
        if (node == null || node.getNodeType() != JsonNodeType.NUMBER) {
            return null;
        }
        Number num = node.numberValue();
        if (num instanceof Integer) {
            return (Integer) num;
        }
        return Integer.valueOf(num.intValue());
    }

    private static Boolean getNodeValueBool(JsonNode node) {
        if (node == null || node.getNodeType() != JsonNodeType.BOOLEAN) {
            return null;
        }
        return Boolean.valueOf(node.booleanValue());
    }

    public static String readNodeString(JsonNode node, String field) {
        if (node != null) {
            return getNodeValueString(node.get(field));
        }
        return null;
    }

    public static Long readNodeLong(JsonNode node, String field) {
        if (node != null) {
            return getNodeValueLong(node.get(field));
        }
        return null;
    }

    public static Integer readNodeInt(JsonNode node, String field) {
        if (node != null) {
            return getNodeValueInt(node.get(field));
        }
        return null;
    }

    public static Boolean readNodeBool(JsonNode node, String field) {
        if (node != null) {
            return getNodeValueBool(node.get(field));
        }
        return null;
    }

    private static UserLoginData readUserLoginData(JsonNode node) {
        if (node != null) {
            Long id = readNodeLong(node, "id");
            String name = readNodeString(node, "name");
            String privateKey = readNodeString(node, "privateKey");
            if (!(id == null || privateKey == null)) {
                return new UserLoginData(id, name, privateKey);
            }
        }
        return null;
    }

    //
//    private static HeaterLoginData readHeaterLoginData(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Long id = readNodeLong(node, "id");
//            String privateKey = readNodeString(node, "privateKey");
//            if (!(id == null || privateKey == null)) {
//                return new HeaterLoginData(id, privateKey);
//            }
//        }
//        return null;
//    }
//
//    private static ServiceVersionInfo readServiceVersionInfo(JsonNode node, long dataModelVersionWanted) {
//        if (node != null) {
//            Long minVersion = readNodeLong(node, "minVersion");
//            Long latestVersion = readNodeLong(node, "latestVersion");
//            Long currentVersion = readNodeLong(node, "currentVersion");
//            Long dataModelVersion = readNodeLong(node, "dataModelVersion");
//            if (!(currentVersion == null || dataModelVersion == null)) {
//                ServiceVersionInfo result = new ServiceVersionInfo();
//                result.setMinVersion(minVersion);
//                result.setLatestVersion(latestVersion);
//                result.setCurrentVersion(currentVersion);
//                result.setDataModelVersion(dataModelVersion);
//                return result;
//            }
//        }
//        return null;
//    }
//
    private static ClientErrorHolder readClientErrorHolder(JsonNode node) {
        if (node != null) {
            Long errorTypeId = readNodeLong(node, "errorTypeId");
            if (errorTypeId != null) {
                ClientErrorHolder result = new ClientErrorHolder();
                result.setErrorTypeId(errorTypeId.longValue());
                return result;
            }
        }
        return null;
    }

    //
    private static HeaterInfo readHeaterInfo(JsonNode node) {
        if (node != null) {
            Long id = readNodeLong(node, "id");
            String name = readNodeString(node, "name");
            String ip = readNodeString(node, "ip");
            String firmware = readNodeString(node, "firmware");
            String hardware = readNodeString(node, "hardware");
            Integer reportingInterval = readNodeInt(node, "reportingInterval");
            Integer currentTemperature = readNodeInt(node, "currentTemperature");
            Integer targetTemperature = readNodeInt(node, "targetTemperature");
            Integer calibrationTemperature = readNodeInt(node, "calibrationTemperature");
            Long lastSeenL = readNodeLong(node, "lastSeen");
            Timestamp lastSeen = lastSeenL != null ? new Timestamp(lastSeenL.longValue()) : null;
            Long heatingMode = readNodeLong(node, "heatingMode");
            Long zone = readNodeLong(node, "zone");
            String zoneName = readNodeString(node, "zoneName");
            Long schedule = readNodeLong(node, "schedule");
            Boolean hasFirmwareUpdate = readNodeBool(node, "hasFirmwareUpdate");
            if (!(reportingInterval == null || targetTemperature == null)) {
                HeaterInfo result = new HeaterInfo(targetTemperature.intValue(), reportingInterval.intValue());
                result.setId(id);
                result.setName(name);
                result.setIp(ip);
                result.setFirmware(firmware);
                result.setHardware(hardware);
                result.setCalibrationTemperature(calibrationTemperature != null ? Integer.valueOf(calibrationTemperature.intValue()) : null);
                result.setCurrentTemperature(currentTemperature);
                result.setLastSeen(lastSeen);
                result.setHeatingMode(heatingMode);
                result.setZone(zone);
                result.setZoneName(zoneName);
                result.setSchedule(schedule);
                result.setHasFirmwareUpdate(hasFirmwareUpdate);
                return result;
            }
        }
        return null;
    }

    private static Zone readZone(JsonNode node) {
        if (node == null) {
            return null;
        }
        Long id = readNodeLong(node, "id");
        String name = readNodeString(node, "name");
        Boolean away = readNodeBool(node, "away");
        Long awayMode = readNodeLong(node, "awayMode");
        Long awayTillL = readNodeLong(node, "awayTill");
        Timestamp awayTill = awayTillL != null ? new Timestamp(awayTillL.longValue()) : null;
        Long currentHeatingMode = readNodeLong(node, "currentHeatingMode");
        Integer currentTemperature = readNodeInt(node, "currentTemperature");
        Long scheduledHeatingMode = readNodeLong(node, "scheduledHeatingMode");
        String heatingModeName = readNodeString(node, "heatingModeName");
        Long heatingModeTillL = readNodeLong(node, "heatingModeTill");
        Timestamp heatingModeTill = heatingModeTillL != null ? new Timestamp(heatingModeTillL.longValue()) : null;
        Integer temperatureCalibration = readNodeInt(node, "temperatureCalibration");
        Long scheduleId = readNodeLong(node, "scheduleId");
        String scheduleName = readNodeString(node, "scheduleName");
        Integer scheduleTargetTemperature = readNodeInt(node, "scheduleTargetTemperature");
        Integer targetTemperature = readNodeInt(node, "targetTemperature");
        Integer toHour = readNodeInt(node, "toHour");
        Integer toMinute = readNodeInt(node, "toMinute");
        Integer toWeekDay = readNodeInt(node, "toWeekDay");
        Zone result = new Zone();
        result.setId(id);
        result.setName(name);
        result.setAway(away != null ? away.booleanValue() : false);
        result.setAwayMode(awayMode);
        result.setAwayTill(awayTill);
        result.setCurrentHeatingMode(currentHeatingMode);
        result.setCurrentTemperature(currentTemperature);
        result.setScheduledHeatingMode(scheduledHeatingMode);
        result.setHeatingModeName(heatingModeName);
        result.setHeatingModeTill(heatingModeTill);
        result.setTemperatureCalibration(temperatureCalibration);
        result.setScheduleId(scheduleId);
        result.setScheduleName(scheduleName);
        result.setScheduleTargetTemperature(scheduleTargetTemperature);
        result.setTargetTemperature(targetTemperature);
        result.setToHour(toHour);
        result.setToMinute(toMinute);
        result.setToWeekDay(toWeekDay);
        return result;
    }
//
//    private static Schedule readSchedule(JsonNode node, long dataModelVersion) {
//        Timestamp updateTimeStamp = null;
//        if (node == null) {
//            return null;
//        }
//        Long id = readNodeLong(node, "id");
//        String name = readNodeString(node, "name");
//        String code = readNodeString(node, "code");
//        Integer defaultTemperature = readNodeInt(node, "defaultTemperature");
//        Long updateTimeStampL = readNodeLong(node, "updateTimeStamp");
//        if (updateTimeStampL != null) {
//            updateTimeStamp = new Timestamp(updateTimeStampL.longValue());
//        }
//        Long zoneId = readNodeLong(node, "zoneId");
//        String zoneName = readNodeString(node, "zoneName");
//        Long intervalsCount = readNodeLong(node, "intervalsCount");
//        Schedule result = new Schedule();
//        result.setId(id);
//        result.setName(name);
//        result.setCode(code);
//        result.setDefaultTemperature(defaultTemperature);
//        result.setUpdateTimeStamp(updateTimeStamp);
//        result.setZoneId(zoneId);
//        result.setZoneName(zoneName);
//        result.setIntervalsCount(intervalsCount);
//        return result;
//    }
//
//    private static HeatingMode readHeatingMode(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Long id = readNodeLong(node, "id");
//            String name = readNodeString(node, "name");
//            Integer targetTemperature = readNodeInt(node, "targetTemperature");
//            Long color = readNodeLong(node, "color");
//            Boolean sameTemperatureForAllZones = readNodeBool(node, "sameTemperatureForAllZones");
//            if (color != null) {
//                HeatingMode result = new HeatingMode();
//                result.setId(id);
//                result.setName(name);
//                result.setTargetTemperature(targetTemperature);
//                result.setColor(color.longValue());
//                result.setSameTemperatureForAllZones(sameTemperatureForAllZones != null ? sameTemperatureForAllZones.booleanValue() : false);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static ScheduleInterval readScheduleInterval(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Long id = readNodeLong(node, "id");
//            Long mode = readNodeLong(node, "mode");
//            Integer targetTemperature = readNodeInt(node, "targetTemperature");
//            Integer fromHour = readNodeInt(node, "fromHour");
//            Integer fromMinute = readNodeInt(node, "fromMinute");
//            Integer fromWeekDay = readNodeInt(node, "fromWeekDay");
//            Integer toHour = readNodeInt(node, "toHour");
//            Integer toMinute = readNodeInt(node, "toMinute");
//            Integer toWeekDay = readNodeInt(node, "toWeekDay");
//            if (!(mode == null || fromHour == null || fromMinute == null || fromWeekDay == null || toHour == null || toMinute == null || toWeekDay == null)) {
//                ScheduleInterval result = new ScheduleInterval();
//                result.setId(id);
//                result.setMode(mode.longValue());
//                result.setTargetTemperature(targetTemperature);
//                result.setFromHour(fromHour.intValue());
//                result.setFromMinute(fromMinute.intValue());
//                result.setFromWeekDay(fromWeekDay.intValue());
//                result.setToHour(toHour.intValue());
//                result.setToMinute(toMinute.intValue());
//                result.setToWeekDay(toWeekDay.intValue());
//                result.setFromTime(ScheduleInterval.getIntervalTime(result.getFromWeekDay(), result.getFromHour(), result.getFromMinute()));
//                result.setToTime(ScheduleInterval.getIntervalTime(result.getToWeekDay(), result.getToHour(), result.getToMinute()));
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static HeaterInfoForDevice readHeaterInfoForDevice(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Long id = readNodeLong(node, "id");
//            Long updateInterval = readNodeLong(node, "updateInterval");
//            Boolean away = readNodeBool(node, "away");
//            Long awayTillL = readNodeLong(node, "awayTill");
//            Timestamp awayTill = awayTillL != null ? new Timestamp(awayTillL.longValue()) : null;
//            Long activeSchedule = readNodeLong(node, "activeSchedule");
//            Integer scheduleTargetTemperature = readNodeInt(node, "scheduleTargetTemperature");
//            Long currentTimeL = readNodeLong(node, "currentTime");
//            Timestamp currentTime = currentTimeL != null ? new Timestamp(currentTimeL.longValue()) : null;
//            Integer targetTemperature = readNodeInt(node, "targetTemperature");
//            Long awayHeatingMode = readNodeLong(node, "awayHeatingMode");
//            Integer awayTemperature = readNodeInt(node, "awayTemperature");
//            Integer calibrationTemperature = readNodeInt(node, "calibrationTemperature");
//            List<Schedule> schedules = null;
//            JsonNode schedulesNode = node.get("schedules");
//            if (schedulesNode != null && schedulesNode.isArray()) {
//                List<Schedule> arrayList = new ArrayList(schedulesNode.size());
//                Iterator<JsonNode> it = node.iterator();
//                while (it.hasNext()) {
//                    Schedule scheduleI = readSchedule((JsonNode) it.next(), dataModelVersion);
//                    if (scheduleI != null) {
//                        arrayList.add(scheduleI);
//                    }
//                }
//            }
//            if (updateInterval != null) {
//                HeaterInfoForDevice result = new HeaterInfoForDevice();
//                result.setId(id);
//                result.setUpdateInterval(updateInterval.longValue());
//                result.setAway(away != null ? away.booleanValue() : false);
//                result.setAwayTill(awayTill);
//                result.setActiveSchedule(activeSchedule);
//                result.setScheduleTargetTemperature(scheduleTargetTemperature);
//                result.setCurrentTime(currentTime);
//                result.setTargetTemperature(targetTemperature);
//                result.setAwayHeatingMode(awayHeatingMode);
//                result.setAwayTemperature(awayTemperature);
//                result.setCalibrationTemperature(calibrationTemperature);
//                result.setSchedules(schedules);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static DeviceDescriptor readDeviceDescriptor(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            JsonNode versionNode = node.get("version");
//            JsonNode deviceNode = node.get("device");
//            DeviceDescriptorDevice device = null;
//            if (deviceNode != null) {
//                Long id = readNodeLong(deviceNode, "id");
//                String product = readNodeString(deviceNode, "product");
//                String manufacturer = readNodeString(deviceNode, "manufacturer");
//                device = new DeviceDescriptorDevice();
//                device.setId(id);
//                device.setProduct(product);
//                device.setManufacturer(manufacturer);
//            }
//            DeviceDescriptorVersion version = null;
//            if (versionNode != null) {
//                String hardware = readNodeString(versionNode, "hardware");
//                String firmware = readNodeString(versionNode, "firmware");
//                String sdk = readNodeString(versionNode, "sdk");
//                String user_bin = readNodeString(versionNode, "user_bin");
//                version = new DeviceDescriptorVersion();
//                version.setHardware(hardware);
//                version.setFirmware(firmware);
//                version.setSdk(sdk);
//                version.setUser_bin(user_bin);
//            }
//            if (device != null) {
//                DeviceDescriptor result = new DeviceDescriptor();
//                result.setDevice(device);
//                result.setVersion(version);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static HeaterScheduleIntervals readHeaterScheduleIntervals(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Timestamp updateTimeStamp;
//            Long scheduleId = readNodeLong(node, "scheduleId");
//            Long updateTimeStampL = readNodeLong(node, "updateTimeStamp");
//            if (updateTimeStampL != null) {
//                updateTimeStamp = new Timestamp(updateTimeStampL.longValue());
//            } else {
//                updateTimeStamp = null;
//            }
//            List<ScheduleInterval> intervals = null;
//            JsonNode intervalsNode = node.get("intervals");
//            if (intervalsNode != null && intervalsNode.isArray()) {
//                intervals = new ArrayList(intervalsNode.size());
//                Iterator<JsonNode> it = node.iterator();
//                while (it.hasNext()) {
//                    ScheduleInterval intervalI = readScheduleInterval((JsonNode) it.next(), dataModelVersion);
//                    if (intervalI != null) {
//                        intervals.add(intervalI);
//                    }
//                }
//            }
//            if (scheduleId != null) {
//                HeaterScheduleIntervals result = new HeaterScheduleIntervals();
//                result.setScheduleId(scheduleId);
//                result.setUpdateTimeStamp(updateTimeStamp);
//                result.setIntervals(intervals);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static HeaterScheduleIntervalsBinary readHeaterScheduleIntervalsBinary(JsonNode node, long dataModelVersion) {
//        if (node != null) {
//            Long scheduleId = readNodeLong(node, "scheduleId");
//            String intervals = readNodeString(node, "intervals");
//            if (scheduleId != null) {
//                HeaterScheduleIntervalsBinary result = new HeaterScheduleIntervalsBinary();
//                result.setScheduleId(scheduleId.longValue());
//                result.setIntervals(intervals);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static Firmware readFirmware(JsonNode node, long dataModelVersion, ObjectMapper objectMapper) {
//        if (node != null) {
//            Long id = readNodeLong(node, "id");
//            String version = readNodeString(node, "version");
//            String hardwareVersion = readNodeString(node, "hardwareVersion");
//            Object dataBase64 = readNodeString(node, "data");
//            byte[] data = dataBase64 != null ? (byte[]) objectMapper.convertValue(dataBase64, byte[].class) : null;
//            String hash = readNodeString(node, "hash");
//            String signature = readNodeString(node, "signature");
//            Boolean available = readNodeBool(node, "available");
//            Integer type = readNodeInt(node, "type");
//            Long createTimeStampL = readNodeLong(node, "createTimeStamp");
//            Timestamp createTimeStamp = createTimeStampL != null ? new Timestamp(createTimeStampL.longValue()) : null;
//            if (id != null) {
//                Firmware result = new Firmware();
//                result.setId(id);
//                result.setVersion(version);
//                result.setHardwareVersion(hardwareVersion);
//                result.setHash(hash);
//                result.setSignature(signature);
//                result.setAvailable(available);
//                result.setCreateTimeStamp(createTimeStamp);
//                result.setData(data);
//                result.setType(type);
//                return result;
//            }
//        }
//        return null;
//    }
//
//    private static TemperaturesLog readTemperaturesLog(JsonNode node, long dataModelVersion, ObjectMapper objectMapper) {
//        if (node != null) {
//            Timestamp logDate;
//            Long id = readNodeLong(node, "id");
//            Long heaterId = readNodeLong(node, "heaterId");
//            Integer temperature = readNodeInt(node, "temperature");
//            Long logDateL = readNodeLong(node, "logDate");
//            if (logDateL != null) {
//                logDate = new Timestamp(logDateL.longValue());
//            } else {
//                logDate = null;
//            }
//            Integer targetTemperature = readNodeInt(node, "targetTemperature");
//            if (id != null) {
//                TemperaturesLog result = new TemperaturesLog();
//                result.setId(id);
//                result.setHeaterId(heaterId);
//                result.setTemperature(temperature);
//                result.setLogDate(logDate);
//                result.setTargetTemperature(targetTemperature);
//                return result;
//            }
//        }
//        return null;
//    }

    public Map<String, Object> parseGeneralJson(String jsonStr) throws IOException {
        return (Map) getObjectMapper().readValue(jsonStr, new TypeReference<HashMap<String, Object>>() {
        });
    }

    public JsonNode readTree(InputStream in) throws JsonProcessingException, IOException {
        return getObjectMapper().readTree(in);
    }

    public JsonNode readTree(String str) throws JsonProcessingException, IOException {
        return getObjectMapper().readTree(str);
    }

    private <T> void writeAppendList(List<T> list, Class<T> aClass, StringBuilder builder) throws JsonProcessingException {
        builder.append("[");
        if (list != null) {
            boolean isFirst = true;
            for (T itemI : list) {
                String itemJson = writeSpecificItem(itemI, aClass);
                if (itemJson != null) {
                    if (!isFirst) {
                        builder.append(",");
                    }
                    builder.append(itemJson);
                    isFirst = false;
                }
            }
        }
        builder.append("]");
    }

    public <T> String writeSpecificList(List<T> list, Class<T> aClass) throws JsonProcessingException {
        StringBuilder builder = new StringBuilder();
        writeAppendList(list, aClass, builder);
        return builder.toString();
    }

    private <T> List<T> parseSpecificList(JsonNode node, Class<T> aClass, ObjectMapper objectMapper) throws JsonProcessingException {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            List<T> resultGen = new ArrayList();
            Iterator<JsonNode> it = node.iterator();
            while (it.hasNext()) {
                T contentI = parseSpecificItem((JsonNode) it.next(), aClass, objectMapper);
                if (contentI == null) {
                    resultGen = null;
                    break;
                }
                resultGen.add(contentI);
            }
            return resultGen;
        }
        T content = parseSpecificItem(node, aClass, objectMapper);
        if (content == null) {
            return null;
        }
        List<T> result = new ArrayList(1);
        result.add(content);
        return result;
    }

    public <T> List<T> parseSpecificList(String jsonStr, Class<T> aClass, long dataModelVersion) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        return parseSpecificList(objectMapper.readTree(jsonStr), aClass, objectMapper);
    }

    public <T> List<T> parseCommonJSONResponseString(JsonNode treeRoot, Class<T> aClass, boolean isExpectError) throws IOException {

        List<T> result = null;
        ClientErrorHolder readErrorHolder = null;
        if (treeRoot != null) {

            JsonNode errorNode;
            JsonNode contentNode;
            ObjectMapper objectMapper = getObjectMapper();
            if (isExpectError) {
                if (treeRoot.isArray()) {
                    errorNode = treeRoot.has(0) ? treeRoot.get(0) : null;
                    if (treeRoot.has(1)) {
                        contentNode = treeRoot.get(1);
                    } else {
                        contentNode = null;
                    }
                } else {
                    errorNode = treeRoot;
                    contentNode = null;
                }
            } else if (treeRoot.isArray()) {
                errorNode = treeRoot.has(0) ? treeRoot.get(0) : null;
                contentNode = treeRoot.has(1) ? treeRoot.get(1) : null;
            } else {
                Long errorCodeIfNotArray = readNodeLong(treeRoot,"errorTypeId");

                if (errorCodeIfNotArray != null && errorCodeIfNotArray != 0) {
                    raiseError(errorCodeIfNotArray);
                }

                contentNode = treeRoot;
                errorNode = null;
            }
            if (errorNode != null) {
                readErrorHolder = (ClientErrorHolder) parseSpecificItem(errorNode, ClientErrorHolder.class, objectMapper);
            }
            if (contentNode != null) {
                result = parseSpecificList(contentNode, aClass, objectMapper);
                if (result == null && (readErrorHolder == null || readErrorHolder.getErrorTypeId() == 0)) {
                    readErrorHolder = new ClientErrorHolder(ClientErrorType.ObjectMappingError);
                }
            }

            if (readErrorHolder != null && readErrorHolder.getErrorTypeId() != 0){
                raiseError(readErrorHolder.getErrorTypeId());
            }

        }
        return result;
    }

    private static void raiseError(long errorTypeId) throws IOException {
        throw new IOException(
                String.format("ADAX Cloud reported error code %s: %s ",
                        Long.toString(errorTypeId),
                        ClientErrorType.getById(errorTypeId)));

    }
}
