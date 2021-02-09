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

import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.PARAMETER_ROOM_NAME;
import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.THING_TYPE_ROOM;
import static org.openhab.binding.adaxheater.internal.AdaxHeaterHandlerFactory.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.adaxheater.internal.AdaxAccountHandler;
import org.openhab.binding.adaxheater.internal.AdaxHeaterHandlerFactory;
import org.openhab.binding.adaxheater.publicApi.AdaxAccountData;
import org.openhab.binding.adaxheater.publicApi.AdaxRoom;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdaxDiscoveryService} is responsible for discovering Adax smart heater rooms connected
 * to an account.
 *
 * @author Nicolai Grødum - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.adax")
public class AdaxDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AdaxDiscoveryService.class);

    public AdaxDiscoveryService() {
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
                logger.debug("Retrieving zones for {}", account.getKey());

                AdaxAccountData allData = account.getValue().getClient().getAllData();

                for (AdaxRoom room : allData.rooms) {
                    if (!account.getValue().hasRoom(room.getRoomId())) {
                        addZone(room, account.getKey());
                    }
                }
            } catch (Exception e) {
                logger.warn("could not scan {}", account.getKey(), e);
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
     * @param room
     */
    private void addZone(AdaxRoom room, ThingUID bridgeUID) {

        logger.debug("Adding room {} with id {}.", room.name, room.getRoomId());

        ThingUID uid = new ThingUID(THING_TYPE_ROOM, "" + room.getRoomId());

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PARAMETER_ROOM_NAME, room.name);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withLabel("Adax Room " + room.name).build();

        thingDiscovered(result);
    }
}
