/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.adaxheater.cloudapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The {@link HeaterInfo} maps the HeaterInfo JSON object
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
@JsonIgnoreProperties({"registerTime", "groupId", "macId", "state"})
public class HeaterInfo {
    private Integer calibrationTemperature;
    private Integer currentTemperature;
    private String firmware;
    private Long groupId;
    private String hardware;
    private Boolean hasFirmwareUpdate;
    private Long heatingMode;
    private Long id;
    private String ip;
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp lastSeen;
    private Integer macId;
    private String name;
    private Timestamp registerTime;
    private int reportingInterval;
    private Long schedule;
    private Integer state;
    private int targetTemperature;
    private Long zone;
    private String zoneName;

    public HeaterInfo(int targetTemperature, int reportingInterval) {
        this.targetTemperature = targetTemperature;
        this.reportingInterval = reportingInterval;
    }

    public Integer getCalibrationTemperature() {
        return this.calibrationTemperature;
    }

    public Integer getCurrentTemperature() {
        return this.currentTemperature;
    }

    public String getFirmware() {
        return this.firmware;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public String getHardware() {
        return this.hardware;
    }

    public Boolean getHasFirmwareUpdate() {
        return this.hasFirmwareUpdate;
    }

    public Long getHeatingMode() {
        return this.heatingMode;
    }

    public Long getId() {
        return this.id;
    }

    public String getIp() {
        return this.ip;
    }

    public Timestamp getLastSeen() {
        return this.lastSeen;
    }

    public Integer getMacId() {
        return this.macId;
    }

    public String getName() {
        return this.name;
    }

    public Timestamp getRegisterTime() {
        return this.registerTime;
    }

    public int getReportingInterval() {
        return this.reportingInterval;
    }

    public Long getSchedule() {
        return this.schedule;
    }

    public Integer getState() {
        return this.state;
    }

    public int getTargetTemperature() {
        return this.targetTemperature;
    }

    public Long getZone() {
        return this.zone;
    }

    public String getZoneName() {
        return this.zoneName;
    }

    public void setCalibrationTemperature(Integer calibrationTemperature) {
        this.calibrationTemperature = calibrationTemperature;
    }

    public void setCurrentTemperature(Integer currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public void setHasFirmwareUpdate(Boolean hasFirmwareUpdate) {
        this.hasFirmwareUpdate = hasFirmwareUpdate;
    }

    public void setHeatingMode(Long heatingMode) {
        this.heatingMode = heatingMode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setMacId(Integer macId) {
        this.macId = macId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegisterTime(Timestamp registerTime) {
        this.registerTime = registerTime;
    }

    public void setReportingInterval(int reportingInterval) {
        this.reportingInterval = reportingInterval;
    }

    public void setSchedule(Long schedule) {
        this.schedule = schedule;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setTargetTemperature(int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setZone(Long zone) {
        this.zone = zone;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String toString() {
        return this.targetTemperature + "|" + this.reportingInterval;
    }
}
