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

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Prerequisites {

    private static final Set<String> RESERVED_QUERY_KEYS = new HashSet<>(
        Arrays.asList(new String[]{"auth_key", "auth_timestamp", "auth_version", "auth_signature", "body_md5"}));

    public static void nonNull(final String name, final Object ref) {
        if (ref == null) throw new IllegalArgumentException("Parameter [" + name + "] must not be null");
    }

    public static void maxLength(final String name, final int max, final List<?> ref) {
        if (ref.size() > max) throw new IllegalArgumentException("Parameter [" + name + "] must have size < " + max);
    }

    public static void noReservedKeys(final MultivaluedMap<String, Object> params) {
        for (String k : params.keySet()) {
            if (RESERVED_QUERY_KEYS.contains(k.toLowerCase())) {
                throw new IllegalArgumentException("Query parameter key [" + k + "] is reserved and should not be submitted. It will be generated by the signature generation.");
            }
        }
    }
}
