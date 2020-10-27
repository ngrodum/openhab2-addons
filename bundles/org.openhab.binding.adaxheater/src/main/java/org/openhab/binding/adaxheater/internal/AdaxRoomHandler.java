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
package org.openhab.binding.adaxheater.internal;

import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.CHANNEL_ROOM_CURRENT_TEMP;
import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.CHANNEL_ROOM_TARGET_TEMP;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.adaxheater.publicApi.AdaxRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdaxRoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nicolai Grødum - Initial contribution
 */
public class AdaxRoomHandler extends BaseThingHandler {

    private static Logger logger = LoggerFactory.getLogger(AdaxRoomHandler.class);
    private final int roomId;

    public AdaxRoomHandler(Thing thing) {
        super(thing);
        roomId = Integer.parseInt(thing.getUID().getId());
    }

    public int getRoomId() {
        return roomId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("ADAX handleCommand:" + channelUID + " cmd=" + command);

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        // handleCommand:adaxheater:zone:5584:zoneCurrentTemperature cmd=REFRESH

        try {
            if (command == RefreshType.REFRESH) {
                // schedulePoller(0, true);

                // updateZoneData(getBridgeHandler().getClient().getZone(zoneId));

            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_ROOM_TARGET_TEMP: {

                        try {
                            Integer targetTemp = Integer.parseInt(command.toString());

                            getBridgeHandler().getClient().setRoomTargetTemp(roomId, targetTemp * 100);

                            if (this.getThing().getStatus() != ThingStatus.ONLINE) {
                                updateStatus(ThingStatus.ONLINE);
                            }

                            updateState(channelUID, new DecimalType(targetTemp));
                        } catch (NumberFormatException e) {
                            logger.error("error 2 ", e);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                        } catch (Exception ex) {
                            logger.error("error 3 ", ex);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error 1 ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        // super.initialize();
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // UNKNOWN, ONLINE, OFFLINE or REMOVED allowed!
        updateStatus(ThingStatus.UNKNOWN);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /**
     * If the bridge goes offline, cancels the polling and goes offline. If the bridge goes online, will attempt to
     * re-initialize
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        // logger.info("bridgeStatusChanged " + bridgeStatusInfo);
    }

    private synchronized AdaxAccountHandler getBridgeHandler() {

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("Required bridge not defined for device {}.", getThing());
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private AdaxAccountHandler getBridgeHandler(Bridge bridge) {

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof AdaxAccountHandler) {
            return (AdaxAccountHandler) handler;
        } else {
            logger.warn("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            return null;
        }
    }

    public void updateRoomData(AdaxRoom room) {

        if (room != null) {

            logger.debug("room.getCurrentTemperature() = {} {} C={} T={}", getThing().getStatus(), room.getName(),
                    room.getCurrentTemperature(), room.getTargetTemperature());

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            if (room.getCurrentTemperature() != null) {
                updateState(CHANNEL_ROOM_CURRENT_TEMP, new DecimalType(room.getCurrentTemperature() / 100.0));
            }
            if (room.getTargetTemperature() != null) {
                updateState(CHANNEL_ROOM_TARGET_TEMP, new DecimalType(room.getTargetTemperature() / 100));
            }
        }
    }
}
