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
import javax.json.JsonStructure;
import javax.ws.rs.HttpMethod;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PusherChannel {

    private final Pusher client;
    private final String name;

    public PusherChannel(Pusher client, String name) {
        this.client = client;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Pusher getClient() {
        return client;
    }

    @Override
    public String toString() {
        return name;
    }

    public void publish(String eventName, JsonStructure eventData) throws PusherException {
        Prerequisites.nonNull("eventName", eventName);
        Prerequisites.nonNull("data", eventData);
        JsonObject body = Json.createObjectBuilder()
            .add("channels", Json.createArrayBuilder().add(getName()))
            .add("name", eventName)
            .add("data", eventData.toString())
            .build();
        getClient().request(HttpMethod.POST, "events", body);
    }

}
