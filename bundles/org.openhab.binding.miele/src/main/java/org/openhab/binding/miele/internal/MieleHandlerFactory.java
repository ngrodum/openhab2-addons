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
package org.openhab.binding.miele.internal;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.miele.internal.discovery.MieleApplianceDiscoveryService;
import org.openhab.binding.miele.internal.handler.CoffeeMachineHandler;
import org.openhab.binding.miele.internal.handler.DishWasherHandler;
import org.openhab.binding.miele.internal.handler.FridgeFreezerHandler;
import org.openhab.binding.miele.internal.handler.FridgeHandler;
import org.openhab.binding.miele.internal.handler.HobHandler;
import org.openhab.binding.miele.internal.handler.HoodHandler;
import org.openhab.binding.miele.internal.handler.MieleApplianceHandler;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler;
import org.openhab.binding.miele.internal.handler.OvenHandler;
import org.openhab.binding.miele.internal.handler.TumbleDryerHandler;
import org.openhab.binding.miele.internal.handler.WashingMachineHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MieleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.miele")
public class MieleHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(MieleBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    MieleApplianceHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (MieleBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID mieleBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, mieleBridgeUID, null);
        }
        if (MieleApplianceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID mieleApplianceUID = getApplianceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, mieleApplianceUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the miele binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (MieleBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            MieleBridgeHandler handler = new MieleBridgeHandler((Bridge) thing);
            registerApplianceDiscoveryService(handler);
            return handler;
        } else if (MieleApplianceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            if (thing.getThingTypeUID().equals(THING_TYPE_HOOD)) {
                return new HoodHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGEFREEZER)) {
                return new FridgeFreezerHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGE)) {
                return new FridgeHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_OVEN)) {
                return new OvenHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_HOB)) {
                return new HobHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_WASHINGMACHINE)) {
                return new WashingMachineHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DRYER)) {
                return new TumbleDryerHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DISHWASHER)) {
                return new DishWasherHandler(thing);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_COFFEEMACHINE)) {
                return new CoffeeMachineHandler(thing);
            }
        }

        return null;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String hostID = (String) configuration.get(HOST);
            thingUID = new ThingUID(thingTypeUID, hostID);
        }
        return thingUID;
    }

    private ThingUID getApplianceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String applianceId = (String) configuration.get(APPLIANCE_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, applianceId, bridgeUID.getId());
        }
        return thingUID;
    }

    private synchronized void registerApplianceDiscoveryService(MieleBridgeHandler bridgeHandler) {
        MieleApplianceDiscoveryService discoveryService = new MieleApplianceDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MieleBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                MieleApplianceDiscoveryService service = (MieleApplianceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }
}
