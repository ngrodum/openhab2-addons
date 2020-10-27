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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AdaxHeaterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nicolai Grodum - Initial contribution
 */
@NonNullByDefault
public class AdaxHeaterBindingConstants {

    private static final String BINDING_ID = "adaxheater";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public final static ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");

    // List of all Zone Thing parameters:
    public static final String PARAMETER_ROOM_NAME = "roomName";

    // List of all Channel ids
    public final static String CHANNEL_ROOM_TARGET_TEMP = "roomTargetTemperature";
    public final static String CHANNEL_ROOM_CURRENT_TEMP = "roomCurrentTemperature";
}
