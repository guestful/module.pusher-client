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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PusherPresence {

    private final String id;
    private final JsonObject attributes;

    public PusherPresence(String id) {
        this.id = id;
        this.attributes = Json.createObjectBuilder().build();
    }

    public PusherPresence(String id, JsonObject attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public JsonObject getAttributes() {
        return attributes;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
            .add("user_id", getId())
            .add("user_info", getAttributes())
            .build();
    }

    @Override
    public String toString() {
        return getId();
    }
}
