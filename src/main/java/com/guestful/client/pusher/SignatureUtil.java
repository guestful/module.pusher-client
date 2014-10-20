/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.client.pusher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.json.JsonStructure;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class SignatureUtil {

    private static final char[] BASE16 = "0123456789abcdef".toCharArray();

    public static WebTarget sign(final String method,
                          WebTarget target,
                          JsonStructure body,
                          MultivaluedMap<String, Object> queryParams,
                          final String key,
                          final String secret) {

        Prerequisites.noReservedKeys(queryParams);

        MultivaluedMap<String, Object> allQueryParams = new MultivaluedHashMap<String, Object>(queryParams);

        target = target.queryParam("auth_key", key);
        allQueryParams.putSingle("auth_key", key);

        target = target.queryParam("auth_version", "1.0");
        allQueryParams.putSingle("auth_version", "1.0");

        String stamp = Long.toString(System.currentTimeMillis() / 1000);
        target = target.queryParam("auth_timestamp", stamp);
        allQueryParams.putSingle("auth_timestamp", stamp);

        if (body != null) {
            String md5 = bodyMd5(body);
            target = target.queryParam("body_md5", md5);
            allQueryParams.putSingle("body_md5", md5);
        }

        // This is where the auth gets a bit weird. The query params for the request must include
        // the auth signature which is a signature over all the params except itself.
        target = target.queryParam("auth_signature", sign(buildSignatureString(method, target.getUri().getPath(), allQueryParams), secret));

        return target;
    }

    public static String sign(final String input, final String secret) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "SHA256"));

            final byte[] digest = mac.doFinal(input.getBytes("UTF-8"));
            return encodeHexString(digest);
        } catch (final InvalidKeyException e) {
            /// We validate this when the key is first provided, so we should never encounter it here.
            throw new RuntimeException("Invalid secret key", e);
        }
        // If either of these doesn't exist, we're pretty much out of luck.
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("The Pusher REST client requires HmacSHA256 support", e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("The Pusher REST client needs UTF-8 support", e);
        }
    }

    private static String bodyMd5(final JsonStructure body) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] digest = md.digest(body.toString().getBytes("UTF-8"));
            return encodeHexString(digest);
        }
        // If this doesn't exist, we're pretty much out of luck.
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("The Pusher REST client requires MD5 support", e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("The Pusher REST client needs UTF-8 support", e);
        }
    }

    // Visible for testing
    private static String buildSignatureString(final String method, final String path, final MultivaluedMap<String, Object> queryParams) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method)
            .append('\n')
            .append(path)
            .append('\n');

        final String[] keys = queryParams.keySet().toArray(new String[queryParams.size()]);
        Arrays.sort(keys);

        boolean first = true;
        for (final String key : keys) {
            if (!first) sb.append('&');
            else first = false;

            sb.append(key)
                .append('=')
                .append(queryParams.getFirst(key));
        }

        return sb.toString();
    }

    private static String encodeHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = BASE16[v >>> 4];
            hexChars[j * 2 + 1] = BASE16[v & 0x0F];
        }
        return new String(hexChars);
    }

}