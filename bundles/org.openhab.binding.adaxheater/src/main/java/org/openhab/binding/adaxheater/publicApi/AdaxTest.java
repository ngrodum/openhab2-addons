/*
 * package org.openhab.binding.adaxheater.publicApi;
 * 
 * import org.eclipse.jdt.annotation.Nullable;
 * import org.eclipse.jetty.client.HttpClient;
 * import org.eclipse.smarthome.auth.oauth2client.internal.OAuthFactoryImpl;
 * import org.eclipse.smarthome.auth.oauth2client.internal.OAuthStoreHandler;
 * import org.eclipse.smarthome.auth.oauth2client.internal.OAuthStoreHandlerImpl;
 * import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
 * import org.eclipse.smarthome.core.storage.StorageService;
 * import org.eclipse.smarthome.io.net.http.HttpClientFactory;
 * import org.eclipse.smarthome.io.net.http.internal.ExtensibleTrustManagerImpl;
 * import org.eclipse.smarthome.io.net.http.internal.WebClientFactoryImpl;
 * import org.eclipse.smarthome.test.storage.VolatileStorageService;
 * import org.slf4j.Logger;
 * import org.slf4j.LoggerFactory;
 * 
 * public class AdaxTest {
 * 
 * 
 * private static final Logger logger = LoggerFactory.getLogger(AdaxTest.class);
 * 
 * private static final String API_URL_TOKEN = "https://api-1.adax.no/client-api/auth/token";
 * 
 * public static void main(String[] args) {
 * WebClientFactoryImpl httpFact = new WebClientFactoryImpl(new ExtensibleTrustManagerImpl());
 * 
 * OAuthFactoryImpl oAuthFactory = new OAuthFactoryImpl() {
 * 
 * @Override
 * public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
 * super.setHttpClientFactory(httpClientFactory);
 * }
 * 
 * @Override
 * public void setOAuthStoreHandler(OAuthStoreHandler oAuthStoreHandler) {
 * super.setOAuthStoreHandler(oAuthStoreHandler);
 * }
 * 
 * @Override
 * public OAuthClientService createOAuthClientService(String handle, String tokenUrl, @Nullable String authorizationUrl,
 * String clientId, @Nullable String clientSecret, @Nullable String scope, @Nullable Boolean supportsBasicAuth) {
 * super.setHttpClientFactory(httpFact);
 * 
 * var ss = new OAuthStoreHandlerImpl() {
 * 
 * @Override
 * public synchronized void setStorageService(StorageService storageService) {
 * super.setStorageService(storageService);
 * }
 * };
 * ss.setStorageService(new VolatileStorageService() {
 * });
 * 
 * super.setOAuthStoreHandler(ss);
 * return super.createOAuthClientService(handle, tokenUrl, authorizationUrl, clientId, clientSecret, scope,
 * supportsBasicAuth);
 * }
 * };
 * 
 * 
 * final HttpClient httpClient = httpFact.getCommonHttpClient();
 * 
 * final OAuthClientService oAuthService = oAuthFactory.createOAuthClientService("yikes",
 * API_URL_TOKEN,
 * API_URL_TOKEN,
 * "oh2",
 * "lu9buXaOyGuhvOXgO",
 * null,
 * true);
 * AdaxClientApi client = new AdaxClientApi(oAuthService,httpClient);
 * 
 * client.getAllZones();
 * }
 * }
 */
