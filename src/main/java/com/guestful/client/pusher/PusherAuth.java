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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class PusherAuth {

    private final String auth;
    private final String channelData;

    /**
     * Private channel constructor
     */
    public PusherAuth(final String key, final String signature) {
        this(key, signature, null);
    }

    /**
     * Presence channel constructor
     */
    public PusherAuth(final String key, final String signature, final String channelData) {
        this.auth = key + ":" + signature;
        this.channelData = channelData;
    }

    public String getAuth() {
        return auth;
    }

    public String getChannelData() {
        return channelData;
    }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("auth", getAuth());
        if (channelData != null) {
            builder.add("channel_data", getChannelData());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return getAuth();
    }
}
