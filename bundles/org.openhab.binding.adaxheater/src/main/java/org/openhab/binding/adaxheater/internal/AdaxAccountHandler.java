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

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.adaxheater.publicApi.AdaxClientApi;
import org.openhab.binding.adaxheater.publicApi.AdaxJwtAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AdaxAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nicolai Grodum - Initial contribution
 */
public class AdaxAccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private @Nullable AdaxClientApi client;
    private @Nullable AdaxAccountPoller poller;
    private final Logger logger = LoggerFactory.getLogger(AdaxAccountHandler.class);

    private final String API_URL_TOKEN = "https://api-1.adax.no/client-api/auth/token";
    private static final int TOKEN_EXPIRES_IN_BUFFER_SECONDS = 120;
    private final Gson gson = new GsonBuilder().create();

    private @Nullable AdaxHeaterConfiguration config;

    public AdaxAccountHandler(Bridge bridge, final OAuthFactory oAuthFactory, final HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    private synchronized AdaxAccountPoller getPoller() {
        if (poller == null) {
            poller = new AdaxAccountPoller(this, scheduler);
        }
        return poller;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("ADAX handleCommand:{}, cmd={}", channelUID, command);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(AdaxHeaterConfiguration.class);
        logger.debug("Got config: {}", config);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        getPoller().schedulePoller(5000, true);

        // Example for background initialization:
        // scheduler.execute(() -> {
        //
        // try {
        // AdaxCloudClient client = this.getClient();
        // if (client.)
        // }
        // boolean thingReachable = true; // <background task with long running initialization here>
        // // when done do:
        //
        // if (thingReachable) {
        // updateStatus(ThingStatus.ONLINE);
        // } else {
        // updateStatus(ThingStatus.OFFLINE);
        // }
        // });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /**
     * Checks bridge configuration. If configuration is valid returns true.
     *
     * @return true if the configuration if valid
     */
    private boolean checkConfig(final AdaxHeaterConfiguration heaterConfig) {
        if (StringUtils.isNotBlank(heaterConfig.password) && StringUtils.isNotBlank(heaterConfig.username)) {
            return true;
        } else {
            logger.debug("Username or password empty.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing configuration.");
            return false;
        }
    }

    public AdaxClientApi getClient() {
        AdaxHeaterConfiguration config = this.config;
        AdaxClientApi client = this.client;

        if (client == null && config != null) {

            if (checkConfig(config)) {
                final OAuthClientService oAuthClientService = oAuthFactory.createOAuthClientService(
                        thing.getUID().getAsString(), API_URL_TOKEN, null, config.username, config.password, null,
                        true);

                oAuthClientService.addAccessTokenRefreshListener(this);

                client = new AdaxClientApi(oAuthClientService, this.httpClient);
                this.client = client;
                logger.warn("************************************************  Initialized client.");
            }
        }

        if (client != null && isApiAuthorized(client.oAuthClientService)) {
            logger.warn("************************************************  Client is authenticated!");

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
        return client;
    }

    public boolean isApiAuthorized(OAuthClientService oAuthClientService) {
        boolean isAuthorized = false;
        try {
            AdaxHeaterConfiguration config = this.config;
            AccessTokenResponse localAccessTokenResponse = oAuthClientService.getAccessTokenResponse();
            if (localAccessTokenResponse != null) {
                logger.trace("API: Got AccessTokenResponse from OAuth service: {}", localAccessTokenResponse);

                if (localAccessTokenResponse.isExpired(LocalDateTime.now(), TOKEN_EXPIRES_IN_BUFFER_SECONDS)
                        || hasTokenExpired(localAccessTokenResponse.getAccessToken(),
                                TOKEN_EXPIRES_IN_BUFFER_SECONDS)) {
                    logger.debug("API: Token is expiring soon. Refreshing it now");
                    localAccessTokenResponse = oAuthClientService.refreshToken();
                    localAccessTokenResponse.setCreatedOn(LocalDateTime.now());// This must be stored in the token!!
                }
                isAuthorized = true;
            } else if (config != null) {
                logger.info("API: Didn't get an AccessTokenResponse from OAuth service - doing auth!!!");
                AccessTokenResponse atr = oAuthClientService
                        .getAccessTokenByResourceOwnerPasswordCredentials(config.username, config.password, null);

                atr.setCreatedOn(LocalDateTime.now());// This must be stored in the token!!
                logger.debug("GOT ATR." + atr);
            }
        } catch (OAuthException | IOException | RuntimeException e) {
            logger.error("API: Got exception trying to get access token from OAuth service", e);
        } catch (OAuthResponseException e) {
            logger.error("API: Exception getting access token: error='{}', description='{}'", e.getError(),
                    e.getErrorDescription());
        }
        return isAuthorized;
    }

    /**
     * Extract the expiry date in the user provided token for the hobby API. Log warnings and errors if the token is
     * close to expiry or expired.
     *
     * @return true if token has expired or cannot be verifed
     */
    private boolean hasTokenExpired(String token, int secondsBuffer) {

        AdaxJwtAccessToken jwtToken = null;

        String[] tokenArray = token.split("\\.");

        if (tokenArray.length == 3) {
            String tokenPayload = new String(Base64.getDecoder().decode(tokenArray[1]));

            logger.debug("Adax: JWT token payload {}", tokenPayload);

            try {
                jwtToken = gson.fromJson(tokenPayload, AdaxJwtAccessToken.class);
            } catch (JsonSyntaxException e) {
                logger.debug("Adax: unexpected token payload {}", tokenPayload);
            } catch (NoSuchElementException ignore) {
                // Ignore if exp not present in response, this should not happen in token payload response
                logger.trace("Adax: no expiry date found in payload {}", tokenPayload);
            }
        }

        if (jwtToken != null) {

            logger.debug("Adax: jwtToken.exp {} {} {} {}", jwtToken.exp, jwtToken.iat, jwtToken.iss, jwtToken.sub);
            Date expiryDate;
            try {
                long epoch = jwtToken.exp * 1000; // convert to milliseconds
                expiryDate = new Date(epoch);
            } catch (NumberFormatException e) {
                logger.debug("Adax: token expiry not valid {}", jwtToken.exp);
                return false;
            }

            Date now = new Date();
            if (expiryDate.before(DateUtils.addSeconds(now, secondsBuffer))) {
                logger.warn("Adax: API token expired, was valid until {}",
                        DateFormat.getDateInstance().format(expiryDate));
                return true;
            } else {
                logger.info("Adax: API token still valid, valid until {}",
                        DateFormat.getDateInstance().format(expiryDate));
            }
            return false;
        }

        return false;
    }

    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail, String message) {
        updateStatus(status, statusDetail, message);
    }

    public boolean hasRoom(int roomId) {

        List<Thing> things = getThing().getThings();

        return !things.isEmpty()
                && things.stream().anyMatch(t -> t.getThingTypeUID().equals(AdaxHeaterBindingConstants.THING_TYPE_ROOM)
                        && t.getUID().getId() == String.valueOf(roomId));
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse accessTokenResponse) {
        logger.error("account handler onAccessTokenResponse" + accessTokenResponse);
    }
}
