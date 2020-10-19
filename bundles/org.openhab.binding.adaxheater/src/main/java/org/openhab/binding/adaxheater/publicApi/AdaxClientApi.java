package org.openhab.binding.adaxheater.publicApi;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.auth.AuthenticationException;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class AdaxClientApi {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String BEARER = "Bearer ";
    private static final String CONTENT_TYPE = "application/json";
    private static final String API_URL = "https://api-1.adax.no/client-api";

    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 10;
    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    public final OAuthClientService oAuthClientService;
    private final HttpClient httpClient;

    private final Logger logger = LoggerFactory.getLogger(AdaxClientApi.class);

    public AdaxClientApi(final OAuthClientService oAuthService, final HttpClient httpClient) {
        this.oAuthClientService = oAuthService;
        this.httpClient = httpClient;
    }

    public AdaxAccountData getAllData() {
        try {
            AdaxAccountData data = executeGet(API_URL + "/rest/v1/content/", AdaxAccountData.class);

            logger.info("got data:" + data);

            logger.info("got data rooms:" + data.rooms);

            logger.info("got data homes:" + data.homes);

            logger.info("got data devices:" + data.devices);

            return data;
        } catch (IOException e) {
            logger.error("Error getting all data (io)", e);
            e.printStackTrace();
        } catch (AuthenticationException e) {
            logger.error("Error getting all data (auth)", e);
        } catch (AdaxClientApiException e) {
            logger.error("Error getting all data (api)", e);
        }
        return null;
    }

    public void setRoomTargetTemp(int roomId, int temp) {
    }

    /**
     * Executes a HTTP GET request with default headers and returns data as object of type T.
     *
     * @param url
     * @param clazz type of data to return
     * @return
     * @throws IOException
     * @throws AuthenticationException
     * @throws AdaxClientApiException
     */
    private <T> T executeGet(final String url, final Class<T> clazz)
            throws IOException, AuthenticationException, AdaxClientApiException {

        logger.error("Geting:" + url);

        final ContentResponse response = request(httpClient.newRequest(url).method(HttpMethod.GET));

        logger.error("Got content:" + response.getContentAsString());

        return gson.fromJson(response.getContentAsString(), clazz);
    }

    private ContentResponse request(final Request request)
            throws IOException, AuthenticationException, AdaxClientApiException {
        final ContentResponse response;
        try {
            final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

            logger.error("Getting atr:" + accessTokenResponse);
            logger.error("Getting access token:" + accessTokenResponse.getAccessToken());

            response = request.header(HttpHeader.ACCEPT, CONTENT_TYPE)
                    .header(HttpHeader.AUTHORIZATION, BEARER + accessTokenResponse.getAccessToken())
                    .timeout(HTTP_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {

            logger.error("YIKES WHILE DOING REQUEST !!!!!! :" + e);

            throw new IOException(e);
        }
        handleResponseErrors(response, request.getURI());
        return response;
    }

    private AccessTokenResponse getAccessTokenResponse() throws AuthenticationException, IOException {
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthClientService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            throw new AuthenticationException("Error fetching access token: " + e.getMessage());
        }
        if (accessTokenResponse == null || StringUtils.isBlank(accessTokenResponse.getAccessToken())) {
            throw new AuthenticationException("No Adax accesstoken. Is this thing authorized?");
        }
        return accessTokenResponse;
    }

    /**
     * Handles errors from the {@link ContentResponse} and throws the following errors:
     *
     * @param response
     * @param uri uri of api call made
     * @throws IOException
     * @throws AdaxClientApiException
     * @throws AuthenticationException
     */
    private void handleResponseErrors(final ContentResponse response, final URI uri)
            throws IOException, AdaxClientApiException, AuthenticationException {
        String content = "";

        switch (response.getStatus()) {
            case HttpStatus.OK_200:
                logger.debug("Statuscode is OK: [{}]", uri);
                return;
            case HttpStatus.SERVICE_UNAVAILABLE_503:
                logger.debug("innogy service is unavailabe (503).");
                throw new AdaxClientApiException("Adax service is unavailabe (503).");
            default:
                logger.debug("Statuscode {} is NOT OK: [{}]", response.getStatus(), uri);
                try {
                    content = response.getContentAsString();
                    logger.trace("Response error content: {}", content);
                    // final ErrorResponse error = gson.fromJson(content, ErrorResponse.class);

                    logger.debug("Error unparsed JSON message, code: {} / message: {}", response.getStatus(),
                            response.getReason());
                    throw new AdaxClientApiException("Error code: " + response.getStatus());
                } catch (final JsonSyntaxException e) {
                    throw new AdaxClientApiException("Invalid JSON syntax in error response: " + content);
                }
        }
    }
}
