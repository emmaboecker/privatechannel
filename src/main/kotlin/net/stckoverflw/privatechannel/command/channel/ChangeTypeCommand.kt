package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.any
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.suspendLazy
import net.stckoverflw.privatechannel.ChannelType
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.changeTypeCommand() = ephemeralSubCommand(::ChangeTypeArguments) {
    name = "type"
    description = "Change the type of this channel"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)
        if (hasAccessToPrivateChannel(privateChannel)) {
            val guildSettings = suspendLazy { PrivateChannelDatabase.getSettingsForGuild(safeGuild.id) }
            val guild = suspendLazy { safeGuild.asGuild() }
            when (ChannelType.valueOf(arguments.newType)) {
                ChannelType.PERMANENT -> {
                    if (
                        guildSettings().permanentChannelRoles.isEmpty() ||
                        guildSettings().permanentChannelRoles
                            .any { neededRole ->
                                guild().getMemberOrNull(privateChannel!!.owner)?.roles?.let { it.any { role ->
                                    role.id == neededRole
                                }} == true
                            }
                    ) {
                        PrivateChannelDatabase.privateChannelCollection.save(
                            privateChannel!!.copy(
                                type = ChannelType.PERMANENT
                            )
                        )
                    } else {
                        respond {
                            content = translateString("commands.channel.change-type.no-access")
                        }
                        return@action
                    }
                }
                ChannelType.TEMPORARY -> {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel!!.copy(
                            type = ChannelType.TEMPORARY
                        )
                    )
                }
            }

            respond {
                content = translateString("commands.channel.change-type.success", arguments.newType.lowercase())
            }
        }
    }
}

class ChangeTypeArguments : Arguments() {
    val newType by stringChoice(
        "type", "The Type the channel should be changed to",
        mapOf(
            "Temporary" to ChannelType.TEMPORARY.name,
            "Permanent" to ChannelType.PERMANENT.name,
        )
    )
}
