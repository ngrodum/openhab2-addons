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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.adaxheater.cloudapi.AdaxCloudClient;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@link AdaxHeaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nicolai Grodum - Initial contribution
 */
public class AdaxAccountHandler extends BaseBridgeHandler {

    private @Nullable AdaxCloudClient client;
    private final BundleContext bundleContext;
    private @Nullable AdaxAccountPoller poller;
    private final Logger logger = LoggerFactory.getLogger(AdaxHeaterHandler.class);

    private @Nullable AdaxHeaterConfiguration config;

    public AdaxAccountHandler(Bridge bridge) {
        super(bridge);
        this.bundleContext = FrameworkUtil.getBundle(AdaxAccountHandler.class).getBundleContext();
    }

    private synchronized AdaxAccountPoller getPoller() {
        if (poller == null){
            poller = new AdaxAccountPoller(this, scheduler);
        }
        return poller;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("ADAX handleCommand:" + channelUID + " cmd=" + command);
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(AdaxHeaterConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);


        getPoller().schedulePoller(5000, true);


        // Example for background initialization:
        // scheduler.execute(() -> {
        //
        //     try {
        //         AdaxCloudClient client = this.getClient();
        //         if (client.)
        //     }
        //     boolean thingReachable = true; // <background task with long running initialization here>
        //     // when done do:
        //
        //     if (thingReachable) {
        //         updateStatus(ThingStatus.ONLINE);
        //     } else {
        //         updateStatus(ThingStatus.OFFLINE);
        //     }
        // });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public AdaxCloudClient getClient() {
        if (this.client == null){
          //  BigDecimal loginId = (BigDecimal) getConfig().get(PARAMETER_ACCOUNT_USERID);
          //  String privateKey = (String) getConfig().get(PARAMETER_ACCOUNT_PRIVATEKEY);

            if (config.privateKey == null || StringUtils.isEmpty(config.privateKey)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                client = new AdaxCloudClient(config.loginId.longValue(), config.privateKey, this.bundleContext);
                updateStatus(ThingStatus.ONLINE);
                //TODO: Do a proper online/offline check now and future..
                logger.info("Initialized successfully.");
            }
        }

        return this.client;
    }

    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail, String message) {
        updateStatus(status, statusDetail, message);
    }

    public boolean hasZone(long zoneId) {

        List<Thing> things = getThing().getThings();

        return !things.isEmpty() && things.stream()
                                          .anyMatch(t -> t.getThingTypeUID().equals(AdaxHeaterBindingConstants.THING_TYPE_ZONE)
                                                         && StringUtils.isNumeric(t.getUID().getId())
                                                         && Long.valueOf(t.getUID().getId()).longValue() == zoneId);

    }

    public boolean hasHeater(long heaterId) {

        List<Thing> things = getThing().getThings();

        return !things.isEmpty() && things.stream()
                                          .anyMatch(t -> t.getThingTypeUID().equals(AdaxHeaterBindingConstants.THING_TYPE_HEATER)
                                                         && StringUtils.isNumeric(t.getUID().getId())
                                                         && Long.valueOf(t.getUID().getId()).longValue() == heaterId);

    }
}


