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

import static org.openhab.binding.adaxheater.internal.AdaxHeaterBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdaxHeaterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nicolai Grodum - Initial contribution
 */

@Component(configurationPid = "binding.adaxheater", service = ThingHandlerFactory.class)
public class AdaxHeaterHandlerFactory extends BaseThingHandlerFactory {

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private Logger logger = LoggerFactory.getLogger(AdaxHeaterHandlerFactory.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ACCOUNT);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ROOM);
    }

    @Activate
    public AdaxHeaterHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference HttpClientFactory httpClientFactory) {
        this.oAuthFactory = oAuthFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    public final static HashMap<ThingUID, AdaxAccountHandler> accountHandlers = new HashMap(1);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        logger.info("Adax createHandler:{}", thing);

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            AdaxAccountHandler accountHandler = new AdaxAccountHandler((Bridge) thing, oAuthFactory, httpClient);
            accountHandlers.put(thing.getUID(), accountHandler);
            return accountHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ROOM)) {
            return new AdaxRoomHandler(thing);
        } else {
            logger.error("Unknown thing type requested: {}", thingTypeUID);
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AdaxAccountHandler) {
            accountHandlers.remove(thingHandler.getThing().getUID());
        }
    }
}
