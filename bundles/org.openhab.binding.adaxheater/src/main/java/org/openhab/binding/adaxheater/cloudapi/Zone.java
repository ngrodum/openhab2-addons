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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * The {@link Zone} represents an Adax Zone
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
public class Zone {
    private boolean away;
    private Long awayMode;
    private Integer awayTemperature;
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp awayTill;
    private Long currentHeatingMode;
    private Integer currentTemperature;
    private String heatingModeName;
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp heatingModeTill;
    private Long id;
    private String name;
    private Long scheduleId;
    private String scheduleName;
    private Integer scheduleTargetTemperature;
    private Long scheduledHeatingMode;
    private Integer targetTemperature;
    private Integer temperatureCalibration;
    private Integer toHour;
    private Integer toMinute;
    private Integer toWeekDay;

    private static int getDay(Calendar calendar) {
        int n2 = 7;
        switch (calendar.get(7)) {
            default: {
                n2 = 0;
            }
            case 1: {
                return n2;
            }
            case 2: {
                return 1;
            }
            case 3: {
                return 2;
            }
            case 4: {
                return 3;
            }
            case 5: {
                return 4;
            }
            case 6: {
                return 5;
            }
            case 7:
        }
        return 6;
    }

    public Long getAwayMode() {
        return this.awayMode;
    }

    public Integer getAwayTemperature() {
        return this.awayTemperature;
    }

    public Timestamp getAwayTill() {
        return this.awayTill;
    }

    public Long getCurrentHeatingMode() {
        return this.currentHeatingMode;
    }

    public Integer getCurrentTemperature() {
        return this.currentTemperature;
    }

    @Deprecated
    public Long getHeatingMode() {
        return this.scheduledHeatingMode;
    }

    public String getHeatingModeName() {
        return this.heatingModeName;
    }

    public Timestamp getHeatingModeTill() {
        return this.heatingModeTill;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Long getScheduledHeatingMode() {
        return this.scheduledHeatingMode;
    }

    public Long getScheduleId() {
        return this.scheduleId;
    }

    public String getScheduleName() {
        return this.scheduleName;
    }

    public Integer getScheduleTargetTemperature() {
        return this.scheduleTargetTemperature;
    }

    public Integer getTargetTemperature() {
        return this.targetTemperature;
    }

    public Integer getTemperatureCalibration() {
        return this.temperatureCalibration;
    }

    public Integer getToHour() {
        return this.toHour;
    }

    public Integer getToMinute() {
        return this.toMinute;
    }

    public Integer getToWeekDay() {
        return this.toWeekDay;
    }

    public boolean isAway() {
        return this.away;
    }

    public void setAway(boolean away) {
        this.away = away;
    }

    public void setAwayMode(Long awayMode) {
        this.awayMode = awayMode;
    }

    public void setAwayTemperature(Integer awayTemperature) {
        this.awayTemperature = awayTemperature;
    }

    public void setAwayTill(Timestamp awayTill) {
        this.awayTill = awayTill;
    }

    public void setCurrentHeatingMode(Long currentHeatingMode) {
        this.currentHeatingMode = currentHeatingMode;
    }

    public void setCurrentTemperature(Integer currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    @Deprecated
    public void setHeatingMode(Long heatingMode) {
        this.scheduledHeatingMode = heatingMode;
    }

    public void setHeatingModeName(String heatingModeName) {
        this.heatingModeName = heatingModeName;
    }

    public void setHeatingModeTill(Timestamp heatingModeTill) {
        this.heatingModeTill = heatingModeTill;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScheduledHeatingMode(Long scheduledHeatingMode) {
        this.scheduledHeatingMode = scheduledHeatingMode;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public void setScheduleTargetTemperature(Integer scheduleTargetTemperature) {
        this.scheduleTargetTemperature = scheduleTargetTemperature;
    }

    public void setTargetTemperature(Integer targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setTemperatureCalibration(Integer temperatureCalibration) {
        this.temperatureCalibration = temperatureCalibration;
    }

    public void setToHour(Integer toHour) {
        this.toHour = toHour;
    }

    public void setToMinute(Integer toMinute) {
        this.toMinute = toMinute;
    }

    public void setToWeekDay(Integer toWeekDay) {
        this.toWeekDay = toWeekDay;
    }
}
