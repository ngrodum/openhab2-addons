package org.openhab.binding.adaxheater.cloudapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class SigningKeys {
    private KeyFactory cachedDSAKeyFactory;
    private static Logger logger = LoggerFactory.getLogger(SigningKeys.class);

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private MessageDigest cachedSha1MessageDigest;

    public String signData(String privateKey, Object... data) {
        try {
            KeyFactory keyFactory = getKeyFactoryDSA();
            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initSign(keyFactory.generatePrivate(new PKCS8EncodedKeySpec(hexStringToByteArray(privateKey))));
            sig.update(encodeToBigEndianBytes(data));
            return byteArrayToHexString(sig.sign(), true);
        } catch (Throwable ex) {

            ex.printStackTrace();

            logger.warn("failed to sign data", ex);
            throw new IllegalArgumentException("privateKey");
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s == null)
            return null;

        return DatatypeConverter.parseHexBinary(s.replaceAll(":", ""));
    }

    public static String byteArrayToHexString(byte[] bytes, boolean insertColons) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < bytes.length) {
            if (insertColons && i != 0) {
                builder.append(":");
            }
            int v = bytes[i] & 0xFF;
            builder.append(hexArray[v >>> 4]);
            builder.append(hexArray[v & 0x0F]);
            i++;
        }
        return builder.toString();
    }

    private KeyFactory getKeyFactoryDSA() throws NoSuchAlgorithmException {
        if (this.cachedDSAKeyFactory == null) {
            this.cachedDSAKeyFactory = KeyFactory.getInstance("DSA");
        }
        return this.cachedDSAKeyFactory;
    }

    public byte[] encodeToBigEndianBytes(Object... data) throws Exception {
        int i = 0;

        int bufferSize = 0;
        int length = data.length;
        while (i < length) {
            Object obj = data[i];
            if (obj != null) {
                if (obj instanceof Long) {
                    bufferSize += 8;
                } else if (obj instanceof Integer) {
                    bufferSize += 4;
                } else if (obj instanceof String) {
                    bufferSize += ((String) obj).length() * 2;
                } else if (obj instanceof Byte) {
                    bufferSize++;
                } else if (obj instanceof byte[]) {
                    bufferSize += ((byte[]) obj).length;
                } else {
                    throw new IllegalArgumentException("Unsupported data type: " + obj.getClass().toString());
                }
            }
            i++;
        }

        if (bufferSize == 0) {
            return new byte[0];
        }
        i = 0;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.BIG_ENDIAN);
        while (i < length) {
            Object obj = data[i];
            if (obj != null) {
                if (obj instanceof Long) {
                    buffer.putLong(((Long) obj).longValue());
                } else if (obj instanceof Integer) {
                    buffer.putInt(((Integer) obj).intValue());
                } else if (obj instanceof String) {
                    String text = (String) obj;
                    for (int i2 = 0; i2 < text.length(); i2++) {
                        buffer.putChar(text.charAt(i2));
                    }
                } else if (obj instanceof Byte) {
                    buffer.put(((Byte) obj).byteValue());
                } else if (obj instanceof byte[]) {
                    buffer.put((byte[]) obj);
                }
            }
            i++;
        }
        return buffer.array();
    }

    private MessageDigest getMessageDigestSha1() throws NoSuchAlgorithmException {
        if (this.cachedSha1MessageDigest == null) {
            this.cachedSha1MessageDigest = MessageDigest.getInstance("SHA-1");
        }
        return this.cachedSha1MessageDigest;
    }

    public byte[] getSha1(byte[] data) {
        try {
            MessageDigest digest = getMessageDigestSha1();
            digest.reset();
            digest.update(data);
            return digest.digest();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public byte[] getSha1AsciiOnly(String... data) {
        try {
            byte[] bytes;
            int length = data.length;
            if (length == 0) {
                bytes = new byte[0];
            } else if (length == 1) {
                bytes = data[0].getBytes("US-ASCII");
            } else {
                StringBuilder builder = new StringBuilder();
                for (String append : data) {
                    builder.append(append);
                }
                bytes = builder.toString().getBytes("US-ASCII");
            }
            return getSha1(bytes);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
