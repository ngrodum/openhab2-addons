/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdaxHeaterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nicolai Grodum - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.adaxheater", service = ThingHandlerFactory.class)
public class AdaxHeaterHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(AdaxHeaterHandlerFactory.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ACCOUNT);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_ZONE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_HEATER);
    }

    public final static HashMap<ThingUID, AdaxAccountHandler> accountHandlers = new HashMap(1);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        logger.info("Adax createHAndler:{}", thing);

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            AdaxAccountHandler accountHandler = new AdaxAccountHandler((Bridge) thing);
            accountHandlers.put(thing.getUID(), accountHandler);
            return accountHandler;
        } else if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new AdaxZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HEATER)) {
            return new AdaxHeaterHandler(thing);
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
