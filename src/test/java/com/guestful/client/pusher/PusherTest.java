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

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public class PusherTest {

    @Test
    @Ignore
    public void test() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        LoggerFactory.getILoggerFactory();

        Client restClient = ClientBuilder.newBuilder().build();
        restClient.register(JsonProcessingFeature.class);

        Pusher pusher = new Pusher(restClient, System.getenv("PUSHER_URL"));

        PusherPresence presence = new PusherPresence("okoMskkfJxo9JcOzF7Bpiw", Json.createObjectBuilder()
            .add("id", "okoMskkfJxo9JcOzF7Bpiw")
            .add("email", "mathieu@guestful.com")
            .add("locale", "en")
            .add("name", "Mathieu Carbou")
            .add("device", "Support")
            .build());
        PusherAuth auth = pusher.authenticate("39345.292710", "presence-restaurant-Dk_0YMqYt8ZszESJoDl0ig", presence);
        System.out.println(auth.toJson());
        assertEquals("5919132c793349d259d5:a2039c5af8a797ddb3c4963403b2885b93a05c70bb226f206674e63bdb868ac8", auth.getAuth());

        pusher.getChannel("support-restaurant-Dk_0YMqYt8ZszESJoDl0ig").publish("eval", Json.createObjectBuilder().add("code", "alert('Support message from CI');").build());

        //pusher.getChannel("support-restaurant-Dk_0YMqYt8ZszESJoDl0ig").publish("eval", Json.createObjectBuilder().add("code", "document.location.reload();").build());
    }

}
