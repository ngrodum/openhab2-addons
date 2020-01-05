package org.openhab.binding.adaxheater.cloudapi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import static org.openhab.binding.adaxheater.cloudapi.AdaxWebClient.parseJsonResponse;

/**
 * Created by ngrodum on 27/03/2017.
 */
public class AdaxDirectClient {
    private static Logger logger = LoggerFactory.getLogger(AdaxDirectClient.class);

    private final String ip;
    private final String password;
    private final String clientHost;

    private boolean useBasicAuthSigning = true; //firmware not newer than 1.0.1
    private boolean useSignUrlParam = true; //FW > 1.0.0.17 and lt 1.0.1

    public AdaxDirectClient(String ip, String password) {
        this.ip = ip;
        this.password = password;
        this.useBasicAuthSigning = true;
        this.clientHost = "http://" + ip + "/client";
    }

    private static final String HTTP_PARAM_CHUNK_COUNT = "chunkCount";
    private static final String HTTP_PARAM_CHUNK_NR = "chunkNr";
    private static final String HTTP_PARAM_COMMAND = "command";
    private static final String HTTP_PARAM_ID = "id";
    private static final String HTTP_PARAM_INTERVAL_COUNT = "intervalCount";
    private static final String HTTP_PARAM_KEY = "key";
    private static final String HTTP_PARAM_MODE = "mode";
    private static final String HTTP_PARAM_PSK = "psk";
    private static final String HTTP_PARAM_SCHEDULE = "schedule";
    private static final String HTTP_PARAM_SCHEDULE_INTERVALS = "intervals";
    private static final String HTTP_PARAM_SIGN = "sign";
    private static final String HTTP_PARAM_SIGNATURE = "signature";
    private static final String HTTP_PARAM_SSID = "ssid";
    private static final String HTTP_PARAM_TILL = "till";
    private static final String HTTP_PARAM_TIME = "time";
    private static final String HTTP_PARAM_VALUE = "value";
    private static final String HTTP_PARAM_VALUE2 = "value2";

    private static final String COMMAND_ADD_HEATING_MODE = "add_mode";
    private static final String COMMAND_ADD_SCHEDULE = "add_schedule";
    private static final String COMMAND_GET_HEATING_MODES = "heating_modes";
    private static final String COMMAND_GET_INFO = "info";
    private static final String COMMAND_GET_SCHEDULES = "schedules";
    private static final String COMMAND_GET_SCHEDULE_INTERVALS = "schedule_intervals";
    private static final String COMMAND_GET_STATUS = "status";
    private static final String COMMAND_GET_TIME = "get_time";
    private static final String COMMAND_GET_WITH_LOCALE = "get_withlocale";
    private static final String COMMAND_REFRESH_DATA = "refresh_data";
    private static final String COMMAND_REMOVE_HEATING_MODE = "remove_mode";
    private static final String COMMAND_REMOVE_SCHEDULE = "remove_schedule";
    private static final String COMMAND_SET_ACTIVE_SCHEDULE = "change_schedule";
    private static final String COMMAND_SET_AWAY = "set_away";
    private static final String COMMAND_SET_AWAY_HEATING_MODE = "set_away_mode";
    private static final String COMMAND_SET_CALIBRATION_TEMP = "set_calibration_temp";
    private static final String COMMAND_SET_CLOUD_PARAMS = "save_key_and_finish";
    private static final String COMMAND_SET_FIRMWARE_CHUNK = "data";
    private static final String COMMAND_SET_FIRMWARE_END = "reset";
    private static final String COMMAND_SET_FIRMWARE_START = "start";
    private static final String COMMAND_SET_HEATING_MODE_COLOR = "set_mode_color";
    private static final String COMMAND_SET_HEATING_MODE_NAME = "set_mode_name";
    private static final String COMMAND_SET_HEATING_MODE_TEMP = "set_mode_temperature";
    private static final String COMMAND_SET_NAME = "set_name";
    private static final String COMMAND_SET_PASSWORD = "set_password";
    private static final String COMMAND_SET_SCHEDULE_INTERVALS = "set_schedule_intervals";
    private static final String COMMAND_SET_SCHEDULE_INTERVALS_CHUNK = "set_chunked_intervals_chunk";
    private static final String COMMAND_SET_SCHEDULE_INTERVALS_CHUNKS_START = "start_chunked_intervals";
    private static final String COMMAND_SET_SCHEDULE_NAMES = "set_schedule_names";
    private static final String COMMAND_SET_START_BLINK = "start_leds_blink";
    private static final String COMMAND_SET_STOP_BLINK = "stop_leds_blink";
    private static final String COMMAND_SET_TARGET_TEMP = "set_target_temperature";
    private static final String COMMAND_SET_TIME = "set_time";
    private static final String COMMAND_SET_WIFI_PARAMS = "join_wifi";
    private static final String COMMAND_SET_WITH_LOCALE = "set_withlocale";

    public static final int HEATER_READ_LONG_TIMEOUT_MS = 15000;
    public static final int HEATER_READ_TIMEOUT_MS = 10000;
    public static final int HEATER_CONNECTION_TIMEOUT_MS = 5000;

    private static final SigningKeys keys = new SigningKeys();

    private static void appendParam(StringBuilder builder, String key, String value, boolean isFirst) {
        if (isFirst) {
            builder.append("?");
        } else {
            builder.append("&");
        }
        builder.append(key);
        builder.append("=");
        builder.append(value);
    }

    private String appendTimeParam(StringBuilder builder) {
        long requestStartTime = System.currentTimeMillis();
        Long time = Long.valueOf(requestStartTime / 1000);

        String encodedTime = time != null ? time.toString() : "";

        if (useBasicAuthSigning)
            appendParam(builder, HTTP_PARAM_TIME, encodedTime, false);

        return encodedTime;
    }

    private static void appendSignParam(StringBuilder builder, String pass) {
        appendParam(builder, HTTP_PARAM_SIGN, pass != null ? pass : "", false);
    }

    private byte[] getBasicAuthBytes(String... data) {
        if (useBasicAuthSigning) {
            return keys.getSha1AsciiOnly(data);
        }
        return null;
    }

    private static HttpURLConnection createConnection(URL url, boolean isPost, boolean isLongTimeout, byte[] basicAuthBytes) throws IOException {
        return createConnection(url, isPost, isLongTimeout ? HEATER_READ_LONG_TIMEOUT_MS : HEATER_READ_TIMEOUT_MS, basicAuthBytes);
    }

    private static HttpURLConnection createConnection(URL url, boolean isPost, int readTimeoutMs, byte[] basicAuthBytes) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(HEATER_CONNECTION_TIMEOUT_MS);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("Connection", "close");
        if (basicAuthBytes != null) {
            conn.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode(basicAuthBytes)).trim());
        }
        if (isPost) {
            conn.setRequestMethod("POST");
        }
        return conn;
    }

    private static <T> T getFirstOrNull(List<T> list) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    private String getGetSingleRequest(String urlString, byte[] basicAuthBytes) {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            conn = createConnection(new URL(urlString), false, false, basicAuthBytes);
           // HTTPDisconnectRunnable timeoutRunnable = new HTTPDisconnectRunnable(conn);
           // this.uiHandler.postDelayed(timeoutRunnable, (long) (this.timeoutConnectionMs + this.timeoutReadMs));
            int code = conn.getResponseCode();
          //  timeoutRunnable.cancel();
            inputStream = conn.getInputStream();
            if (code == 200) {
                StringBuilder str = new StringBuilder();
                Scanner s = new Scanner(inputStream);
                while (s.hasNext()) {
                    str.append(s.next());
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
                if (conn == null) {
                    return str.toString();
                }
                conn.disconnect();
                return str.toString();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc2) {
                    exc2.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
            return null;
        } catch (SocketTimeoutException exc3) {
            exc3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc22) {
                    exc22.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (SocketException exc4) {
            exc4.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc222) {
                    exc222.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (IOException exc5) {
            exc5.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc2222) {
                    exc2222.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (RuntimeException exc6) {
            exc6.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc22222) {
                    exc22222.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc222222) {
                    exc222222.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return "";
    }

    private static <T> java.util.List<T> getGetRequest(String requestStr, boolean r24, boolean isLongTimeout, byte[] authBytes, java.lang.Class<T> expectedResponseClass) {

        try {
            URL url = new URL(requestStr);
            HttpURLConnection con = createConnection(url, false, isLongTimeout, authBytes);

           /* con.setDoOutput(true);

            DataOutputStream dos = new java.io.DataOutputStream(con.getOutputStream());

            dos.writeBytes(getParamListAsString(params));

            dos.flush();
            dos.close();*/

            logger.info("response=" + con.getResponseCode());

            return parseJsonResponse(con.getInputStream(), false, expectedResponseClass);

        } catch (MalformedURLException mfUrlEx) {
            logger.error("Malformed request:" + requestStr, mfUrlEx);
            throw new RuntimeException("Malformed request:" + requestStr);
        } catch (IOException ioex) {
            logger.error("Failed request:" + requestStr, ioex);
            throw new RuntimeException("Failed request:" + requestStr);
        }


    }

    public boolean writeRemoteQuickUpdate() throws IOException {


        StringBuilder builder = new StringBuilder();
        builder.append(clientHost);

        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_REFRESH_DATA, true);

        String encodedTime = appendTimeParam(builder);

     //   if (useSignUrlParam)
        appendSignParam(builder, password);


      //  String request = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_REFRESH_DATA, encodedTime, password));

        List<Boolean> res = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_REFRESH_DATA, encodedTime, password), Boolean.class);

        return getFirstOrNull(res);
    }


    public String getRemoteDeviceDescriptor() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);
        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_GET_INFO, true);

        String res = getGetSingleRequest(builder.toString(), null);
     //   List<DeviceDescriptor> result = getGetRequest(builder.toString(), false, false, null, DeviceDescriptor.class, this.dataModelVersion, errorHolder);
      //  return (result == null || result.size() != 1) ? null : (DeviceDescriptor) result.get(0);
        return res;
    }

    public Zone getRemoteStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);
        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_GET_STATUS, true);
        String encodedTime = appendTimeParam(builder);
     //   appendSignParam(builder, password);

       String res = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_GET_STATUS, encodedTime, password));

        List<Zone> result = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_GET_STATUS, encodedTime, password), Zone.class);
        return getFirstOrNull(result);
    }

    public Object getTime() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);
        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_GET_TIME, true);
        String encodedTime = appendTimeParam(builder);
        appendSignParam(builder, password);

        String res = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_GET_TIME, encodedTime, password));

        Object result = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_GET_TIME, encodedTime, password), Long.class);
       // return getFirstOrNull(result);
        return result;
    }

    public Boolean startBlink() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);
        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_SET_START_BLINK, true);
        String encodedTime = appendTimeParam(builder);
           appendSignParam(builder, password);

       // String res = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_SET_START_BLINK, encodedTime, password));

        List<Boolean>  result = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_SET_START_BLINK, encodedTime, password), Boolean.class);
        return getFirstOrNull(result);
    }

    public Boolean stopBlink() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);
        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_SET_STOP_BLINK, true);
        String encodedTime = appendTimeParam(builder);
           appendSignParam(builder, password);

     //   String res = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_SET_STOP_BLINK, encodedTime, password));

        List<Boolean>  result = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_SET_STOP_BLINK, encodedTime, password), Boolean.class);
        return getFirstOrNull(result);
    }

    public Boolean writeRemoteTargetTemp(Integer targetTemp) {
        String encodedTargetTemp = targetTemp.toString();
        StringBuilder builder = new StringBuilder();
        builder.append(this.clientHost);

        appendParam(builder, HTTP_PARAM_COMMAND, COMMAND_SET_TARGET_TEMP, true);
        appendParam(builder, HTTP_PARAM_VALUE, encodedTargetTemp, false);
        String encodedTime = appendTimeParam(builder);
        appendSignParam(builder, password);

      //  String res = getGetSingleRequest(builder.toString(), getBasicAuthBytes(COMMAND_SET_STOP_BLINK, encodedTime, password));

        List<Boolean>  result = getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_SET_TARGET_TEMP , encodedTargetTemp, encodedTime, password), Boolean.class);


        return getFirstOrNull(result);

      //  return unboxUpdateResult(getGetRequest(builder.toString(), true, false, getBasicAuthBytes(COMMAND_SET_TARGET_TEMP, encodedTargetTemp, encodedTime, getBasicAuthAdditionalObject()), Boolean.class, this.dataModelVersion, errorHolder));
    }
}

