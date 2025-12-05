package com.ionapi.npc.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches player skins from Mojang API.
 */
public final class SkinFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Map<String, SkinData> CACHE = new ConcurrentHashMap<>();

    private SkinFetcher() {}

    /**
     * Fetches skin data for a player name.
     * Results are cached.
     *
     * @param playerName the player name
     * @return future containing skin data, or null if not found
     */
    @NotNull
    public static CompletableFuture<@Nullable SkinData> fetchSkin(@NotNull String playerName) {
        // Check cache first
        SkinData cached = CACHE.get(playerName.toLowerCase());
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get UUID from name
                String uuid = fetchUUID(playerName);
                if (uuid == null) return SkinData.STEVE;

                // Get profile with textures
                return fetchProfile(uuid, playerName);
            } catch (Exception e) {
                return SkinData.STEVE;
            }
        });
    }

    @Nullable
    private static String fetchUUID(@NotNull String playerName) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(UUID_URL + playerName).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() != 200) return null;

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.get("id").getAsString();
        }
    }

    @Nullable
    private static SkinData fetchProfile(@NotNull String uuid, @NotNull String playerName) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(PROFILE_URL + uuid + "?unsigned=false").toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() != 200) return SkinData.STEVE;

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject properties = json.getAsJsonArray("properties").get(0).getAsJsonObject();

            String texture = properties.get("value").getAsString();
            String signature = properties.has("signature") ? properties.get("signature").getAsString() : "";

            SkinData skin = new SkinData(texture, signature);
            CACHE.put(playerName.toLowerCase(), skin);
            return skin;
        }
    }

    /**
     * Clears the skin cache.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Pre-caches a skin.
     *
     * @param playerName the player name
     * @param skin the skin data
     */
    public static void cache(@NotNull String playerName, @NotNull SkinData skin) {
        CACHE.put(playerName.toLowerCase(), skin);
    }
}
