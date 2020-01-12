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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.adaxheater.cloudapi.AdaxCloudClient;
import org.openhab.binding.adaxheater.cloudapi.HeaterInfo;
import org.openhab.binding.adaxheater.cloudapi.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The {@link AdaxAccountPoller} Responsible for polling the Adax cloud
 *
 * @author Nicolai Grodum - Initial contribution
 */
@NonNullByDefault
public class AdaxAccountPoller {

    private static Logger logger = LoggerFactory.getLogger(AdaxAccountPoller.class);
    private final AdaxAccountHandler bridge;
    private final ScheduledExecutorService scheduler;

    private static final int POLL_INTERVAL_MS = 60000;
    private static final int OFFLINE_POLL_INTERVAL_MS = 120000;

    private @Nullable ScheduledFuture<?> getStatusJob = null;
    private AdaxCloudClient client;

    public AdaxAccountPoller(AdaxAccountHandler bridge, ScheduledExecutorService scheduler) {
        this.bridge = bridge;
        this.scheduler = scheduler;
        this.client = bridge.getClient();
    }

    public synchronized void schedulePoller(int initialDelayMs, boolean online) {

        try {
            if (getStatusJob != null && !getStatusJob.isCancelled()) {
                getStatusJob.cancel(false);
            }
        } catch (Exception e) {
            logger.warn("Failed to cancel getStatusJob:{}", e.getMessage());
        }

        int intervalMs = online ? POLL_INTERVAL_MS : OFFLINE_POLL_INTERVAL_MS;
        getStatusJob = scheduler.scheduleWithFixedDelay(getStatusRunnable, initialDelayMs, intervalMs,
                TimeUnit.MILLISECONDS);
    }


    private Runnable getStatusRunnable = new Runnable() {

        @Override
        public void run() {

            try {

                if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {

                    logger.info("Attempting to go online");

                    readAllZones();
                    readAllHeaters();

                    bridge.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

                    schedulePoller(POLL_INTERVAL_MS, true);
                } else {
                    logger.info("Polling");
                    readAllZones();
                    readAllHeaters();
                }
            } catch (Exception e) {
                logger.error("An exception occurred while communicating with the Adax cloud: '{}'", e.getMessage(), e);
                bridge.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                schedulePoller(OFFLINE_POLL_INTERVAL_MS, false);
            }

        }
    };

    private Map<Long, AdaxHeaterHandler> getAllHeaterHandlers() {
        List<Thing> things = bridge.getThing().getThings();

        return things
                .stream()
                .filter(t -> t.getHandler() instanceof AdaxHeaterHandler)
                .map(t -> (AdaxHeaterHandler) t.getHandler())
                .collect(Collectors.toMap(AdaxHeaterHandler::getHeaterId, p -> p));
    }

    private Map<Long, AdaxZoneHandler> getAllZoneHandlers() {
        List<Thing> things = bridge.getThing().getThings();

        return things
                .stream()
                .filter(t -> t.getHandler() instanceof AdaxZoneHandler)
                .map(t -> (AdaxZoneHandler) t.getHandler())
                .collect(Collectors.toMap(AdaxZoneHandler::getZoneId, p -> p));
    }

    private void readAllZones() {
        try {
            Map<Long, AdaxZoneHandler> allZoneHandlers = getAllZoneHandlers();

            if (!allZoneHandlers.isEmpty()) {

                List<Zone> allZones = getClient().getAllZones();

                allZones
                        .stream()
                        .filter(z -> z.getId() != null)
                        .filter(z -> allZoneHandlers.containsKey(z.getId()))
                        .forEach(zone -> {
                            try {
                                AdaxZoneHandler zoneHandler = allZoneHandlers.get(zone.getId());

                                zoneHandler.updateZoneData(zone);

                            } catch (Exception updateZoneEx) {
                                logger.error("Error updating zone {} ", zone.getId(), updateZoneEx);
                            }
                        });
            }

        } catch (Exception e) {
            logger.error("error reading zones from ADAX.", e);
        }
    }

    private void readAllHeaters() {
        try {
            Map<Long, AdaxZoneHandler> allZoneHandlers = getAllZoneHandlers();
            Map<Long, AdaxHeaterHandler> allHeaterHandlers = getAllHeaterHandlers();

            logger.info("readAllHeaters {} zones, {} heaters.", allZoneHandlers.size(), allHeaterHandlers.size());

            if (!allZoneHandlers.isEmpty() || !allHeaterHandlers.isEmpty()) {

                List<HeaterInfo> allHeaters = getClient().getAllHeaters();

                //update zone handlers:
                Map<AdaxZoneHandler, List<HeaterInfo>> heatersByZone = allHeaters
                        .stream()
                        .filter(h -> h.getZone() != null)
                        .filter(h -> allZoneHandlers.containsKey(h.getZone()))
                        .collect(Collectors.groupingBy(h -> allZoneHandlers.get(h.getZone())));

                for (Map.Entry<AdaxZoneHandler, List<HeaterInfo>> zoneWithHeaters : heatersByZone.entrySet()) {
                    zoneWithHeaters.getKey().updateHeaters(zoneWithHeaters.getValue());
                }

                //Update heater handlers:
                allHeaters
                        .stream()
                        .filter(h -> h.getId() != null)
                        .filter(h -> allHeaterHandlers.containsKey(h.getId()))
                        .forEach(h -> {
                            AdaxHeaterHandler heaterHandler = allHeaterHandlers.get(h.getId());
                            try {
                                heaterHandler.updateHeaterData(h);
                            } catch (Exception updateHeaterEx) {
                                logger.error("Error updating heater {} ", h.getId(), updateHeaterEx);
                            }
                        });
            }

        } catch (Exception e) {
            logger.error("error reading zones from ADAX.", e);
        }
    }


    private AdaxCloudClient getClient() {
        if (this.client == null) {
            this.client = bridge.getClient();
        }
        return this.client;
    }
}
