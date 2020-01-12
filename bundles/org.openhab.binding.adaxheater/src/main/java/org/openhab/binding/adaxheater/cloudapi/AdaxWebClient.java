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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.List;

/**
 * The {@link AdaxWebClient} abstracts web call logic for Adax control
 *
 * @author Nicolai Grodum - Initial contribution 27/03/2017
 */
public abstract class AdaxWebClient {
    private static Logger logger = LoggerFactory.getLogger(AdaxWebClient.class);

    private final static String cloudBaseUrl = "https://sheater.adax.lt/sheater-client-api";
    // private final static String alternativeCloudBaseUrl = "https://sheater.azurewebsites.net/sheater-client-api";
    protected final Long loginId;
    private final String privateKey;
    private SigningKeys keys = new SigningKeys();
    private final SSLSocketFactory sslSocketFactory;

    private static AdaxJsonMarshaller jsonMarshaller = new AdaxJsonMarshaller();

    public AdaxWebClient(Long loginId, String privateKey, URL certificatUrl, String password) {
        this.loginId = loginId;
        this.privateKey = privateKey;

        sslSocketFactory = createSSLSocketFactory(certificatUrl, password);
    }

    protected PairList<String, String> getPostParamListWithSignature(Object... parameters) {
        PairList<String, String> paramList = new PairList();
        paramList.add("signature", getSignature(parameters));
        return paramList;
    }

    private static URL getUrl(String endpoint) {
        try {
            return new URL(cloudBaseUrl + endpoint);
        } catch (MalformedURLException mue) {
            return null;
        }
    }

    private String getSignature(Object... parameters) {
        if (this.privateKey != null) {
            return this.keys.signData(this.privateKey, parameters);
        }
        return null;
    }

    private void setServerCertificate(HttpURLConnection connection) {
        if (!connection.getURL().toString().contains("https://sheater.adax.lt/")) {
            return;
        }
        if (sslSocketFactory != null && (connection instanceof HttpsURLConnection)) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        }
    }

    private static SSLSocketFactory createSSLSocketFactory(URL certificatUrl, String password) {
        try {
            byte[] data;

            InputStream input = certificatUrl.openStream();
            try {
                data = IOUtils.toByteArray(input);
                // process your input here or in separate method
            } catch (Exception readEx) {
                logger.error("read stream ex ", readEx);
                throw readEx;
            } finally {
                input.close();
            }

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new ByteArrayInputStream(data), password.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();

        } catch (Exception exc) {
            logger.error("createSSLSocketFactory", exc);
        }
        return null;
    }

    public static String getParamListAsString(PairList<String, String> paramList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        if (paramList != null && paramList.size() > 0) {
            final String charset = "UTF-8";
            Iterator it = paramList.iterator();
            while (it.hasNext()) {
                Pair<String, String> entity = (Pair) it.next();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("&");
                }
                String str = null;
                String encodedValue = null;
                String key = entity.first;
                String value = entity.second;
                try {
                    str = URLEncoder.encode(key, charset);
                    encodedValue = URLEncoder.encode(value, charset);
                } catch (UnsupportedEncodingException exc) {
                    logger.error("getParamListAsString failed!", exc);
                    throw new IOException("please check your private key format for " + key);
                }
                if (!(str == null || encodedValue == null)) {
                    stringBuilder.append(str);
                    stringBuilder.append("=");
                    stringBuilder.append(encodedValue);
                }
            }
        }
        String result = stringBuilder.toString();
        stringBuilder.setLength(0);
        return result;
    }

    public static String parseJsonResponseStr(InputStream inputStream) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line = bufferedReader.readLine();
        while (line != null) {
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }

        return inputStringBuilder.toString();
    }


    public static <T> List<T> parseJsonResponse(InputStream inputStream, boolean isExpectError, Class<T> aClass) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int chunkSize = 16384;
        byte[] data = new byte[chunkSize];
        while (true) {
            int nRead = inputStream.read(data, 0, data.length);
            if (nRead == -1) {
                break;
            }
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        List<T> result = null;

        byte[] bytes = buffer.toByteArray();

        logger.debug("response: {}", new String(bytes));

        JsonNode treeRoot = jsonMarshaller.readTree(new ByteArrayInputStream(buffer.toByteArray()));
        if (treeRoot != null) {
            result = jsonMarshaller.parseCommonJSONResponseString(treeRoot, aClass, isExpectError);
            logger.debug("response result: {}", result);
        }
        return result;
    }

    protected <T> List<T> postApiRequest(String endpoint, PairList<String, String> params, Class<T> expectedResponseClass) throws IOException {
        try {
            logger.debug("request to {}", endpoint);
            HttpURLConnection con = (HttpURLConnection) getUrl(endpoint).openConnection();
            setServerCertificate(con);

            con.setRequestProperty("Connection", "close");
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            //  con.setConnectTimeout(this.timeoutConnectionMs);
            //  con.setReadTimeout(this.timeoutReadMs);

            con.setDoOutput(true);

            DataOutputStream dos = new java.io.DataOutputStream(con.getOutputStream());

            dos.writeBytes(getParamListAsString(params));

            dos.flush();
            dos.close();

            logger.debug("response={}", con.getResponseCode());

            return parseJsonResponse(con.getInputStream(), false, expectedResponseClass);

        } catch (Exception e) {
            logger.error("postRestRequest ", e);
            throw e;
        }
    }
}
