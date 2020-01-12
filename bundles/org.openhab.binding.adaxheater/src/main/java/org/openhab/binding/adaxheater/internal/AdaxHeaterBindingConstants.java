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
    public final static ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public final static ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");

    //List of all Account Thing parameters:
    // public static final String PARAMETER_ACCOUNT_USEREMAIL = "username";
    // public static final String PARAMETER_ACCOUNT_PASSWORD = "password";
    // public static final String PARAMETER_ACCOUNT_LOGINID = "loginId";
    // public static final String PARAMETER_ACCOUNT_PRIVATEKEY = "privateKey";

    //List of all Zone Thing parameters:
    public static final String PARAMETER_ZONE_NAME = "zoneName";

    //List of all Heater Thing parameters:
    public static final String PARAMETER_HEATER_NAME = "heaterName";


    // List of all Channel ids
    public final static String CHANNEL_ZONE_ONLINE_HEATERS = "zoneOnlineHeaterCount";
    public final static String CHANNEL_ZONE_HEATERS = "zoneHeaterCount";
    public final static String CHANNEL_ZONE_TARGET_TEMP = "zoneTargetTemperature";
    public final static String CHANNEL_ZONE_CURRENT_TEMP = "zoneCurrentTemperature";

    public final static String CHANNEL_HEATER_CURRENT_TEMP = "heaterCurrentTemperature";
    public final static String CHANNEL_HEATER_IP = "heaterIP";
}
