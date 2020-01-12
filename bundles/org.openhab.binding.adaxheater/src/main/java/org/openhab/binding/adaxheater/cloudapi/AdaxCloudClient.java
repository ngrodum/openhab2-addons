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
package org.openhab.binding.adaxheater.cloudapi;

import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * The {@link AdaxCloudClient} communicates with the Adax cloud
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
public class AdaxCloudClient extends AdaxWebClient{

    public AdaxCloudClient(Long loginId, String privateKey, BundleContext context) {
        this(loginId,privateKey, context.getBundle().getResource("/myTrustStore.jks"), "qwerty");
    }

    public AdaxCloudClient(Long loginId, String privateKey, URL resourceURL, String password) {
        super(loginId, privateKey, resourceURL, password);
    }

    private static <T> T getFirstOrNull(List<T> list) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public List<HeaterInfo> getAllHeaters() throws IOException {
        return postApiRequest("/rest/heaters/list/" + this.loginId.toString(), getPostParamListWithSignature(this.loginId), HeaterInfo.class);
    }

    public HeaterInfo getHeater(final long heaterId) throws IOException {
        final List<HeaterInfo> res = this.postApiRequest("/rest/heaters/heater/" + Long.toString(heaterId) + "/" + this.loginId.toString(), this.getPostParamListWithSignature(this.loginId, heaterId), HeaterInfo.class);
        return getFirstOrNull(res);
    }

    public List<Zone> getAllZones() throws IOException {
        return postApiRequest("/rest/zones/list/" + this.loginId.toString(), getPostParamListWithSignature(this.loginId), Zone.class);
    }

    public Zone getZone(long zoneId) throws IOException {
        final List<Zone> res = postApiRequest("/rest/zones/" + Long.toString(zoneId) + "/" + this.loginId.toString(), getPostParamListWithSignature(this.loginId, Long.valueOf(zoneId)), Zone.class);
        return getFirstOrNull(res);
    }

    public Zone setZoneTargetTemp(long zoneId, Integer targetTemp) throws IOException {
        final List<Zone> res = postApiRequest("/rest/zones/" + Long.toString(zoneId) + "/target_temperature/" + this.loginId.toString() + "/" + targetTemp.toString(), getPostParamListWithSignature(Long.valueOf(zoneId), this.loginId, targetTemp), Zone.class);
        return getFirstOrNull(res);
    }

    public UserLoginData registerWithGoogle(String idToken, int timeZoneMinutesOffset, String localeName) throws IOException {
        PairList<String, String> paramList = new PairList();
        paramList.add("idToken", idToken);
        paramList.add("localeName", localeName);
        paramList.add("timeOffset", Integer.toString(timeZoneMinutesOffset));
        List<UserLoginData> res = postApiRequest("/rest/users/google-android/id", paramList, UserLoginData.class);
        return getFirstOrNull(res);
    }

    public UserLoginData loginAdax(String email, String password) throws IOException {
        PairList<String, String> paramList = new PairList();
        paramList.add("login", email);
        paramList.add("password", password);
        List<UserLoginData> res = postApiRequest("/rest/users/login/id", paramList, UserLoginData.class);
        return getFirstOrNull(res);
    }
}

