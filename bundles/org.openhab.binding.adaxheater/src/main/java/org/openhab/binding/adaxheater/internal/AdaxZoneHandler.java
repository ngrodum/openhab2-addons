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

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.binding.adaxheater.cloudapi.AdaxCloudClient;
import org.openhab.binding.adaxheater.cloudapi.HeaterInfo;
import org.openhab.binding.adaxheater.cloudapi.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.*;

/**
 * The {@link AdaxZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nicolai Grødum - Initial contribution
 */
public class AdaxZoneHandler extends BaseThingHandler {

    private static Logger logger = LoggerFactory.getLogger(AdaxZoneHandler.class);
    private AdaxCloudClient client;
    private final Long zoneId;

    private List<HeaterInfo> heaters = new ArrayList<>();

    public AdaxZoneHandler(Thing thing) {
        super(thing);
        zoneId = Long.parseLong(thing.getUID().getId());
    }

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.info("ADAX handleCommand:" + channelUID + " cmd=" + command);

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

//handleCommand:adaxheater:zone:5584:zoneCurrentTemperature cmd=REFRESH

        try {
            if (command == RefreshType.REFRESH) {
                //  schedulePoller(0, true);

                updateZoneData(client.getZone(zoneId));

            } else {
                switch (channelUID.getId()) {
                    case CHANNEL_ZONE_TARGET_TEMP: {

                        try {
                            Integer targetTemp = Integer.parseInt(command.toString());

                            client.setZoneTargetTemp(zoneId, targetTemp * 100);

                            if (this.getThing().getStatus() != ThingStatus.ONLINE) {
                                updateStatus(ThingStatus.ONLINE);
                            }

                            updateState(channelUID, new DecimalType(targetTemp));
                        } catch (NumberFormatException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                        } catch (Exception ex) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        this.client = getBridgeHandler().getClient();

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
        logger.info("bridgeStatusChanged " + bridgeStatusInfo);
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

    public void updateZoneData(Zone z) throws IOException {

        if (z != null) {

            logger.info("z.getCurrentTemperature() = " + getThing().getStatus() + " " + z.getName() + " C=" + z.getCurrentTemperature() + "T=" + z.getTargetTemperature());

            if (getThing().getStatus() != ThingStatus.ONLINE){
                updateStatus(ThingStatus.ONLINE);
            }

            if (z.getCurrentTemperature() != null) {
                updateState(CHANNEL_ZONE_CURRENT_TEMP, new DecimalType(z.getCurrentTemperature() / 100.0));
            }
            if (z.getTargetTemperature() != null) {
                updateState(CHANNEL_ZONE_TARGET_TEMP, new DecimalType(z.getTargetTemperature() / 100));
            }
        }
    }

    public void updateHeaters(List<HeaterInfo> heaters) {

        this.heaters = heaters;

        if (heaters != null) {
            updateState(CHANNEL_ZONE_HEATERS, new DecimalType(heaters.size()));

            long onlineHeaters = heaters.stream().filter(h-> h != null && AdaxHeaterHandler.isHeaterOnline(h)).count();
            updateState(CHANNEL_ZONE_ONLINE_HEATERS, new DecimalType(onlineHeaters));
        }
    }
}
