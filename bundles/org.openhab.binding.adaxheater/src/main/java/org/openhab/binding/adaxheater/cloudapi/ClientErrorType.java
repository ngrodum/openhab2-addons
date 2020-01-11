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

public enum ClientErrorType {
    InvalidSignature(10001),
    SignupInvalidParams(10002),
    SignupDuplicateUser(10003),
    InvalidLoginParameters(10004),
    BadLoginOrPassword(10005),
    BadRemoteServiceReply(10006),
    InvalidSignatureData(10007),
    PasswordResetTimeExpired(10008),
    ConnectionTimeout(10009),
    HeatingModeInUse(20001),
    HeatingModeIsFixed(20002),
    UnregisteredHeatingMode(20003),
    HeaterIsAlreadyRegisterToTheOtherUser(30001),
    UnregisteredHeater(30002),
    NotOwnedHeater(30003),
    InvalidZone(40001),
    ObjectMappingError(50001),
    SQLError(50002),
    UnexpectedState(50003),
    OfflineHeaterNotFound(50004),
    DeviceCommandCancelled(50005),
    ScheduleInUse(60001),
    UnregisteredSchedule(60002),
    UnknownHeaterCommand(70001),
    FirmwareUdateFailed(9001),
    DeviceBusy(9002),
    InvalidParam(9003),
    InvalidWiFiPassword(9004),
    HTTPNotAcceptable(406),
    HTTPInternalServerError(500),
    HTTPForbidden(403),
    HTTPBadRequest(400),
    HTTPServiceNotAvailable(503);
    
    private static ClientErrorType[] allValues;
    private long id;

    private ClientErrorType(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    private static ClientErrorType[] getAllValues() {
        if (allValues == null) {
            allValues = values();
        }
        return allValues;
    }

    public static ClientErrorType getById(long id) {
        for (ClientErrorType typeI : getAllValues()) {
            if (typeI.getId() == id) {
                return typeI;
            }
        }
        return null;
    }
}
