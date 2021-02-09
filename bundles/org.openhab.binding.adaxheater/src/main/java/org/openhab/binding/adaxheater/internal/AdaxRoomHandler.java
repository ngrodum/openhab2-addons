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
        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,

        try {
            if (command == RefreshType.REFRESH) {

            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_ROOM_TARGET_TEMP: {

                        Integer targetTemp = Integer.parseInt(command.toString());

                        getBridgeHandler().getClient().setRoomTargetTemp(roomId, targetTemp * 100);

                        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.ONLINE);
                        }

                        updateState(channelUID, new DecimalType(targetTemp));
                    }
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Handlecommand error ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
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
