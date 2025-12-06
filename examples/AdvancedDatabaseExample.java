package examples;

import com.ionapi.database.IonDatabase;
import com.ionapi.database.IonDatabaseBuilder;
import com.ionapi.database.Transaction;
import com.ionapi.database.annotations.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/**
 * Advanced database example showing:
 * - Entity relationships (@OneToMany, @ManyToOne)
 * - Entity caching (@Cacheable)
 * - Complex queries with transactions
 */
public class AdvancedDatabaseExample extends JavaPlugin {

    private IonDatabase database;

    @Override
    public void onEnable() {
        // Initialize database
        database = IonDatabaseBuilder.sqlite(this)
            .file("data.db")
            .build();

        // Register entities
        database.register(Guild.class);
        database.register(GuildMember.class);
        database.register(PlayerSettings.class);

        getLogger().info("Advanced database example loaded!");
    }

    /**
     * Example: Create a guild with members
     */
    public void createGuild(UUID ownerId, String name) {
        Guild guild = new Guild();
        guild.setId(UUID.randomUUID());
        guild.setName(name);
        guild.setOwnerId(ownerId);
        guild.setCreatedAt(System.currentTimeMillis());

        database.save(guild);

        // Add owner as first member
        GuildMember member = new GuildMember();
        member.setId(UUID.randomUUID());
        member.setPlayerId(ownerId);
        member.setGuildId(guild.getId());
        member.setRank("OWNER");
        member.setJoinedAt(System.currentTimeMillis());

        database.save(member);
    }

    /**
     * Example: Complex query with relationships
     */
    public List<GuildMember> getGuildMembers(UUID guildId) {
        return database.query(GuildMember.class)
            .where("guild_id", guildId)
            .orderBy("joined_at", "ASC")
            .execute();
    }

    /**
     * Example: Transaction with multiple operations
     */
    public boolean transferGuildOwnership(UUID guildId, UUID newOwnerId) {
        try (Transaction tx = database.beginTransaction()) {
            // Update guild owner
            Guild guild = database.query(Guild.class)
                .where("id", guildId)
                .first();

            if (guild == null) {
                tx.rollback();
                return false;
            }

            UUID oldOwnerId = guild.getOwnerId();
            guild.setOwnerId(newOwnerId);
            database.save(guild);

            // Update member ranks
            GuildMember oldOwner = database.query(GuildMember.class)
                .where("guild_id", guildId)
                .and("player_id", oldOwnerId)
                .first();

            GuildMember newOwner = database.query(GuildMember.class)
                .where("guild_id", guildId)
                .and("player_id", newOwnerId)
                .first();

            if (oldOwner != null) {
                oldOwner.setRank("MEMBER");
                database.save(oldOwner);
            }

            if (newOwner != null) {
                newOwner.setRank("OWNER");
                database.save(newOwner);
            }

            tx.commit();
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to transfer ownership: " + e.getMessage());
            return false;
        }
    }

    /**
     * Example: Cached entity access
     */
    public PlayerSettings getSettings(UUID playerId) {
        // First call hits database, subsequent calls use cache
        return database.query(PlayerSettings.class)
            .where("player_id", playerId)
            .first();
    }

    // ==================== ENTITY CLASSES ====================

    /**
     * Guild entity with one-to-many relationship to members.
     */
    @Table("guilds")
    public static class Guild {
        @PrimaryKey
        private UUID id;

        @Column(name = "name", nullable = false, length = 32)
        private String name;

        @Column(name = "owner_id", nullable = false)
        private UUID ownerId;

        @Column(name = "created_at")
        private long createdAt;

        @OneToMany(mappedBy = "guildId", fetch = FetchType.LAZY)
        private List<GuildMember> members;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public List<GuildMember> getMembers() { return members; }
    }

    /**
     * Guild member entity with many-to-one relationship to guild.
     */
    @Table("guild_members")
    public static class GuildMember {
        @PrimaryKey
        private UUID id;

        @Column(name = "player_id", nullable = false)
        private UUID playerId;

        @Column(name = "guild_id", nullable = false)
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "guild_id", referencedColumnName = "id")
        private UUID guildId;

        @Column(name = "rank", length = 16)
        private String rank;

        @Column(name = "joined_at")
        private long joinedAt;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getPlayerId() { return playerId; }
        public void setPlayerId(UUID playerId) { this.playerId = playerId; }
        public UUID getGuildId() { return guildId; }
        public void setGuildId(UUID guildId) { this.guildId = guildId; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
        public long getJoinedAt() { return joinedAt; }
        public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
    }

    /**
     * Cached player settings entity.
     * Settings are cached for 60 seconds to reduce database queries.
     */
    @Table("player_settings")
    @Cacheable(ttl = 60, maxSize = 500)
    public static class PlayerSettings {
        @PrimaryKey
        @Column(name = "player_id")
        private UUID playerId;

        @Column(name = "notifications", defaultValue = "true")
        private boolean notifications;

        @Column(name = "language", length = 5, defaultValue = "'en'")
        private String language;

        @Column(name = "theme", length = 16, defaultValue = "'default'")
        private String theme;

        // Getters and setters
        public UUID getPlayerId() { return playerId; }
        public void setPlayerId(UUID playerId) { this.playerId = playerId; }
        public boolean isNotifications() { return notifications; }
        public void setNotifications(boolean notifications) { this.notifications = notifications; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
    }
}
