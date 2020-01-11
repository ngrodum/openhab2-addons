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
package org.openhab.binding.adaxheater.scanner;

import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.binding.adaxheater.cloudapi.HeaterInfo;
import org.openhab.binding.adaxheater.cloudapi.Zone;
import org.openhab.binding.adaxheater.internal.AdaxAccountHandler;
import org.openhab.binding.adaxheater.internal.AdaxHeaterHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.*;
import static org.openhab.binding.adaxheater.internal.AdaxHeaterHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

/**
 * The {@link ZoneDiscoveryService} is responsible for discovering Adax smart heater zones connected
 * to an account.
 *
 * @author Nicolai Grødum - Initial contribution
 */
public class ZoneDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ZoneDiscoveryService.class);

    public ZoneDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 900, false);
    }

    /**
     * Requests all zones from the ADAX cloud
     *
     */
    @Override
    protected void startScan() {

        logger.debug("Retrieving accounts");

        for (Map.Entry<ThingUID, AdaxAccountHandler> account : AdaxHeaterHandlerFactory.accountHandlers.entrySet()) {

            try {
                logger.info("Retrieving zones for " + account.getKey());
                List<Zone> zones = account.getValue().getClient().getAllZones();

                for (Zone zone : zones) {
                    if (zone.getId() != null && !account.getValue().hasZone(zone.getId().longValue())) {
                        addZone(zone, account.getKey());
                    }
                }

                logger.info("Retrieving heaters for " + account.getKey());
                List<HeaterInfo> heaters = account.getValue().getClient().getAllHeaters();

                for (HeaterInfo heater : heaters) {
                    if (heater.getId() != null && !account.getValue().hasHeater(heater.getId().longValue())) {
                        addHeater(heater, account.getKey());
                    }
                }
            } catch (Exception e) {
                logger.warn("could not scan" + account.getKey(), e);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    /**
     * Submit newly discovered zones.
     *
     * @param zone
     */
    private void addZone(Zone zone, ThingUID bridgeUID) {

        if (zone.getId() == null) {
            logger.info("Ignoring zone without id {}", zone.getName());
            return;
        } else {
            logger.info("Adding zone {} with id {}.", zone.getName(), zone.getId());
        }

        // uid must not contains dots
        ThingUID uid = new ThingUID(THING_TYPE_ZONE, Long.toString(zone.getId()));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PARAMETER_ZONE_NAME, zone.getName());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withLabel("Zone " + zone.getName()).build();

        thingDiscovered(result);
    }

    /**
     * Submit newly discovered heaters.
     *
     * @param heater
     */
    private void addHeater(HeaterInfo heater, ThingUID bridgeUID) {
        if (heater.getId() == null) {
            logger.info("Ignoring heater without id {}", heater.getName());
            return;
        } else {
            logger.info("Adding heater {} with id {}.", heater.getName(), heater.getId());
        }

        // uid must not contains dots
        ThingUID uid = new ThingUID(THING_TYPE_HEATER, Long.toString(heater.getId()));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PARAMETER_HEATER_NAME, heater.getName());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withLabel("Heater " + heater.getName()).build();

        thingDiscovered(result);
    }
}
