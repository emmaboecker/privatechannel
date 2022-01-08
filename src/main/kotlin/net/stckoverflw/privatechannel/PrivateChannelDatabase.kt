package net.stckoverflw.privatechannel

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

object PrivateChannelDatabase : KoinComponent {

    val guildSettingsCollection = database.getCollection<PrivateChannelGuildSettings>("guild_settings")
    val privateChannelCollection = database.getCollection<PrivateChannel>("private_channel")

}

suspend fun PrivateChannel.reFetch() = PrivateChannelDatabase.privateChannelCollection.findOneById(voiceChannelId)!!

suspend fun PrivateChannelDatabase.getSettingsForGuild(guildId: Snowflake): PrivateChannelGuildSettings {
    val guildSettings = guildSettingsCollection.findOneById(guildId)

    return if (guildSettings == null) {
        val newGuildSettings = PrivateChannelGuildSettings(
            guild = guildId
        )
        guildSettingsCollection.save(newGuildSettings)
        newGuildSettings
    } else {
        guildSettings
    }
}

@Serializable
data class PrivateChannel(
    @SerialName("_id") val voiceChannelId: Snowflake,
    val textChannelId: Snowflake,
    val guild: Snowflake,
    val owner: Snowflake,
    val voiceName: String,
    val textName: String,
    val moderators: List<Snowflake> = emptyList(),
    val type: ChannelType = ChannelType.TEMPORARY,
    val access: ChannelAccess = ChannelAccess.PRIVATE,
    val password: String? = null,
    val users: List<Snowflake> = emptyList(),
    val bannedUsers: List<Snowflake> = emptyList(),
) {
    val allowedUsers: List<Snowflake> by lazy { users + moderators + owner }
}

enum class ChannelAccess {
    EVERYONE,
    PRIVATE,
    INVISIBLE
}

enum class ChannelType {
    TEMPORARY,

    // SEMI_PERMANENT,
    PERMANENT,
}


/**
 * @param guild The Guild this settings Entry is for
 * @param createChannel The Channel wich users can create their own channel with
 * @param permanentChannelRoles The Roles needed to create a permanent Channel, empty list if everyone is allowed
 * @param invisibleChannelRoles The Roles needed to create an invisible Channel, empty list if everyone is allowed
 */
@Serializable
data class PrivateChannelGuildSettings(
    @SerialName("_id") val guild: Snowflake,
    val createChannel: Snowflake? = null,
    val privateChannelCategory: Snowflake? = null,
    val permanentChannelRoles: List<Snowflake> = emptyList(),
    val passwordChannelRoles: List<Snowflake> = emptyList(),
    val invisibleChannelRoles: List<Snowflake> = emptyList(),
)
