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
package org.openhab.binding.deconz;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.handler.SensorThermostatThingHandler;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz sensors
 *
 * @author Jan N. Klug - Initial contribution
 * @author Lukas Agethen - Added Thermostat
 */
@NonNullByDefault
public class SensorsTest {
    private @NonNullByDefault({}) Gson gson;

    @Mock
    private @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;

    @Before
    public void initialize() {
        initMocks(this);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Test
    public void carbonmonoxideSensorUpdateTest() throws IOException {
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("carbonmonoxide.json", SensorMessage.class, gson);
        Assert.assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelUID = new ChannelUID(thingUID, "carbonmonoxide");
        Thing sensor = ThingBuilder.create(THING_TYPE_CARBONMONOXIDE_SENSOR, thingUID)
                .withChannel(ChannelBuilder.create(channelUID, "Switch").build()).build();
        SensorThingHandler sensorThingHandler = new SensorThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        sensorThingHandler.messageReceived("", sensorMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID), eq(OnOffType.ON));
    }

    @Test
    public void thermostatSensorUpdateTest() throws IOException {
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("thermostat.json", SensorMessage.class, gson);
        Assert.assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelValveUID = new ChannelUID(thingUID, "valve");
        ChannelUID channelHeatSetPointUID = new ChannelUID(thingUID, "heatsetpoint");
        ChannelUID channelModeUID = new ChannelUID(thingUID, "mode");
        ChannelUID channelTemperatureUID = new ChannelUID(thingUID, "temperature");
        Thing sensor = ThingBuilder.create(THING_TYPE_THERMOSTAT, thingUID)
                .withChannel(ChannelBuilder.create(channelValveUID, "Number").build())
                .withChannel(ChannelBuilder.create(channelHeatSetPointUID, "Number").build())
                .withChannel(ChannelBuilder.create(channelModeUID, "String").build())
                .withChannel(ChannelBuilder.create(channelTemperatureUID, "Number").build()).build();
        SensorThermostatThingHandler sensorThingHandler = new SensorThermostatThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        sensorThingHandler.messageReceived("", sensorMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelValveUID),
                eq(new QuantityType<>(100.0, SmartHomeUnits.PERCENT)));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelHeatSetPointUID),
                eq(new QuantityType<>(25, SIUnits.CELSIUS)));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelModeUID),
                eq(new StringType(ThermostatMode.AUTO.name())));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelTemperatureUID),
                eq(new QuantityType<>(16.5, SIUnits.CELSIUS)));
    }
}
