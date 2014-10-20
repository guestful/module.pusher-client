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

import javax.json.JsonStructure;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class Pusher {

    private static final Logger LOGGER = Logger.getLogger(Pusher.class.getName());
    private static final Pattern URL_PATTERN = Pattern.compile("(https?)://(.+):(.+)@(.+:?.*)/apps/(.+)");

    private final String appId;
    private final String key;
    private final String secret;
    private final WebTarget target;
    private final Client client;

    private boolean enabled = true;

    public Pusher(String uri) {
        this(ClientBuilder.newClient(), uri);
    }

    public Pusher(String appId, String apiKey, String apiSecret) {
        this(ClientBuilder.newClient(), appId, apiKey, apiSecret);
    }

    public Pusher(Client client, String url) {
        Prerequisites.nonNull("url", url);
        this.client = client;
        final Matcher m = URL_PATTERN.matcher(url);
        if (m.matches()) {
            this.key = m.group(2);
            this.secret = m.group(3);
            this.appId = m.group(5);
            this.target = buildWebTarget(m.group(1) + "://" + m.group(4));
        } else {
            throw new IllegalArgumentException("URL '" + url + "' does not match pattern '<scheme>://<key>:<secret>@<host>[:<port>]/apps/<appId>'");
        }
    }

    public Pusher(Client client, String appId, String apiKey, String apiSecret) {
        this.appId = appId;
        this.key = apiKey;
        this.secret = apiSecret;
        this.client = client;
        this.target = buildWebTarget("http://api.pusherapp.com");
    }

    public String getAppId() {
        return appId;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public Client getClient() {
        return client;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected WebTarget buildWebTarget(String base) {
        return getClient().target(base + "/apps/" + getAppId());
    }

    public PusherChannel getChannel(String name) {
        return new PusherChannel(this, name);
    }

    /**
     * Generate authentication response to authorise a user on a presence channel
     * <p>
     * The return value is the complete body which should be returned to a client requesting authorisation.
     */
    public PusherAuth authenticate(final String socketId, final String channel, final PusherPresence user) {
        Prerequisites.nonNull("socketId", socketId);
        Prerequisites.nonNull("channel", channel);
        Prerequisites.nonNull("user", user);

        if (channel.startsWith("private-")) {
            throw new IllegalArgumentException("This method is for presence channels, use authenticate(String, String) to authenticate for a private channel.");
        }
        if (!channel.startsWith("presence-")) {
            throw new IllegalArgumentException("Authentication is only applicable to private and presence channels");
        }
        final String channelData = user.toJson().toString();
        final String signature = SignatureUtil.sign(socketId + ":" + channel + ":" + channelData, secret);
        return new PusherAuth(key, signature, channelData);
    }

    Response request(String method, String path, JsonStructure body) throws PusherException {
        return request(method, path, body, new MultivaluedHashMap<>());
    }

    Response request(String method, String path, JsonStructure body, MultivaluedMap<String, Object> queryParams) throws PusherException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(method + " " + path + " : " + body);
        }
        if (!isEnabled()) {
            return Response.ok().build();
        } else {
            WebTarget t = target.path(path);
            for (Map.Entry<String, List<Object>> entry : queryParams.entrySet()) {
                t = t.queryParam(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
            }
            Response response = SignatureUtil.sign(method, t, body, queryParams, getKey(), getSecret())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .method(method, Entity.entity(body, "application/json; charset=utf-8"));
            if (response.getStatus() != 200) {
                throw new PusherException(response, body);
            }
            return response;
        }
    }

}
