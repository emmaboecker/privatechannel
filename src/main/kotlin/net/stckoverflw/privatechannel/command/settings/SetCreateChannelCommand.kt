package net.stckoverflw.privatechannel.command.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.translateString

suspend fun EphemeralSlashCommand<*>.setCreateChannelCommand() = ephemeralSubCommand(::SetCreateChannelArguments) {
    name = "create-channel"
    description = "Set the Channel wich users can create their own channel by joining it (Voice Channel)"
    guildAdminOnly()

    action {
        val guildSettings = PrivateChannelDatabase.getSettingsForGuild(safeGuild.id)

        if (guildSettings.createChannel != arguments.channel.id) {
            PrivateChannelDatabase.guildSettingsCollection.save(
                guildSettings.copy(
                    createChannel = arguments.channel.id
                )
            )

            respond {
                content = translateString("commands.settings.createchannel.set", arguments.channel.mention)
            }
        } else {
            respond {
                content = translateString("commands.settings.createchannel.error.already", arguments.channel.mention)
            }
        }
    }
}

class SetCreateChannelArguments : Arguments() {
    val channel by channel {
        name = "channel"
        description = "The channel to set the Create-Channel to"
        validate {
            failIf(value.type != ChannelType.GuildVoice, translate("commands.error.invalid_channel.voice"))
        }
    }
}
