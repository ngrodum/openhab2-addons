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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.adaxheater.publicApi.AdaxRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AdaxAccountPoller(AdaxAccountHandler bridge, ScheduledExecutorService scheduler) {
        this.bridge = bridge;
        this.scheduler = scheduler;
    }

    public synchronized void schedulePoller(int initialDelayMs, boolean online) {

        try {
            ScheduledFuture<?> lastJob = getStatusJob;
            if (lastJob != null) {
                if (!lastJob.isCancelled()) {
                    lastJob.cancel(false);
                }
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

                    refreshAllRooms();

                    bridge.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

                    schedulePoller(POLL_INTERVAL_MS, true);
                } else {
                    logger.debug("Polling");
                    refreshAllRooms();
                }
            } catch (Exception e) {
                logger.error("An exception occurred while communicating with the Adax cloud: '{}'", e.getMessage(), e);
                bridge.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                schedulePoller(OFFLINE_POLL_INTERVAL_MS, false);
            }
        }
    };

    private Map<Integer, AdaxRoomHandler> getAllZoneHandlers() {
        List<Thing> things = bridge.getThing().getThings();

        return things.stream().filter(t -> t.getHandler() instanceof AdaxRoomHandler)
                .map(t -> (AdaxRoomHandler) t.getHandler())
                .collect(Collectors.toMap(AdaxRoomHandler::getRoomId, p -> p));
    }

    private void refreshAllRooms() {
        try {

            List<AdaxRoom> allRooms = bridge.getClient().getAllData().rooms;

            Map<Integer, AdaxRoomHandler> allRoomHandlers = getAllZoneHandlers();

            if (!allRoomHandlers.isEmpty()) {

                allRooms.stream().filter(r -> allRoomHandlers.containsKey(r.getRoomId())).forEach(room -> {
                    try {
                        AdaxRoomHandler zoneHandler = allRoomHandlers.get(room.getRoomId());
                        zoneHandler.updateRoomData(room);
                    } catch (Exception updateZoneEx) {
                        logger.error("Error updating room {} ", room.getRoomId(), updateZoneEx);
                    }
                });
            }

        } catch (Exception e) {
            logger.error("error reading rooms from ADAX.", e);
        }
    }
}
