package org.openhab.binding.adaxheater.publicApi;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.core.auth.AuthenticationException;
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

    private static final int TOKEN_EXPIRES_IN_BUFFER_SECONDS = 120;

    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 10;
    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    private final HttpClient httpClient;
    private final String username;
    private final String password;

    private final Logger logger = LoggerFactory.getLogger(AdaxClientApi.class);

    private String accesstoken;

    public AdaxClientApi(final String username, final String password, final HttpClient httpClient) {
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
    }

    public AdaxAccountData getAllData() {
        try {
            AdaxAccountData data = executeGet(API_URL + "/rest/v1/content/", AdaxAccountData.class);

            logger.debug("got data:" + data);

            logger.debug("got data rooms:" + data.rooms);

            logger.debug("got data homes:" + data.homes);

            logger.debug("got data devices:" + data.devices);

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
        try {
            String payload = String.format("{ \"rooms\": [{ \"id\": %d, \"targetTemperature\": %d }] }", roomId, temp);
            AdaxSetTempResponse setTmp = executePost(API_URL + "/rest/v1/control/", payload, "application/json",
                    AdaxSetTempResponse.class);

            logger.debug("setRoomTargetTemp: {}", gson.toJson(setTmp));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AdaxClientApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isApiAuthorized() {
        try {

            if (accesstoken == null || hasTokenExpired(accesstoken, TOKEN_EXPIRES_IN_BUFFER_SECONDS)) {

                accesstoken = getAccessToken(username, password);

                logger.debug("FRESH TOKEN EXPIRED??? " + hasTokenExpired(accesstoken, TOKEN_EXPIRES_IN_BUFFER_SECONDS));

            }
        } catch (Exception e) {
            logger.error("API: Exception getting access token: error='{}', description='{}'", e, e.toString());
        }
        return accesstoken != null && !hasTokenExpired(accesstoken, TOKEN_EXPIRES_IN_BUFFER_SECONDS);
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

            try {
                jwtToken = gson.fromJson(tokenPayload, AdaxJwtAccessToken.class);
            } catch (JsonSyntaxException e) {
                logger.debug("Adax: unexpected token payload {}", tokenPayload);
            } catch (NoSuchElementException ignore) {
                // Ignore if exp not present in response, this should not happen in token payload response
                logger.trace("Adax: no expiry date found in payload {}", tokenPayload);
            }
        } else {
            logger.error("Error/unknown JWT Adax token!" + token);
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
                logger.debug("Adax: API token expired, was valid until {}",
                        DateFormat.getDateInstance().format(expiryDate));
                return true;
            } else {
                logger.debug("Adax: API token still valid, valid until {}",
                        DateFormat.getDateInstance().format(expiryDate));
            }
            return false;
        }

        return false;
    }

    private String getAccessToken(String username, String password) {

        try {
            String payload = String.format("grant_type:password\n" + "password:%s\n" + "username:%s", password,
                    username);

            logger.debug("posting data:" + payload);

            Fields f = new Fields();
            f.add("grant_type", "password");
            f.add("password", this.password);
            f.add("username", this.username);
            FormContentProvider content = new FormContentProvider(f);

            final ContentResponse response = httpClient.newRequest(API_URL + "/auth/token").method(HttpMethod.POST)
                    .content(content, "application/x-www-form-urlencoded").send();

            logger.error("Got content:" + response.getContentAsString());

            AdaxGetTokenResponse data = gson.fromJson(response.getContentAsString(), AdaxGetTokenResponse.class);

            logger.debug("got data:" + data);

            logger.debug("got data rooms SHOULD NOT BE NULL!!!!:" + data.access_token);
            logger.debug("got data rooms:" + data.token_type);
            logger.debug("got data rooms:" + data.expires_in);
            logger.debug("got data rooms:" + data.refresh_token);

            return data.access_token;
        } catch (InterruptedException e) {
            logger.error("Error getting all data (io)", e);
        } catch (ExecutionException e) {
            logger.error("Error getting all data (io)", e);
        } catch (TimeoutException e) {
            logger.error("Error getting all data (io)", e);
        }

        return null;
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

        logger.debug("Geting:" + url);

        final ContentResponse response = request(httpClient.newRequest(url).method(HttpMethod.GET));

        logger.debug("Got content:" + response.getContentAsString());

        return gson.fromJson(response.getContentAsString(), clazz);
    }

    private <T> T executePost(final String url, final String data, final String contentType, final Class<T> clazz)
            throws IOException, AuthenticationException, AdaxClientApiException {

        logger.debug("Posting:" + url);

        final ContentResponse response = request(httpClient.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(data), contentType));

        logger.debug("Got content:" + response.getContentAsString());

        return gson.fromJson(response.getContentAsString(), clazz);
    }

    private ContentResponse request(final Request request)
            throws IOException, AuthenticationException, AdaxClientApiException {
        final ContentResponse response;
        try {

            isApiAuthorized();

            logger.debug("Using access token:" + accesstoken);

            response = request.header(HttpHeader.ACCEPT, CONTENT_TYPE)
                    .header(HttpHeader.AUTHORIZATION, BEARER + accesstoken)
                    .timeout(HTTP_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {

            logger.error("YIKES WHILE DOING REQUEST !!!!!! :" + e);

            throw new IOException(e);
        }
        handleResponseErrors(response, request.getURI());
        return response;
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
                    logger.debug("Response error content: {}", content);
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
