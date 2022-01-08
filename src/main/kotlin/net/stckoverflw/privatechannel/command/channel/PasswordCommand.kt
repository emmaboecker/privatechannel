package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.suspendLazy
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.passwordCommand() = ephemeralSubCommand(::ChannelPasswordArguments) {
    name = "password"
    description = "Change the password of the channel"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)

        if (hasAccessToPrivateChannel(privateChannel)) {
            if (arguments.password != null) {
                val guildSettings = suspendLazy { PrivateChannelDatabase.getSettingsForGuild(safeGuild.id) }
                if (
                    guildSettings().passwordChannelRoles.isEmpty() ||
                    guildSettings().passwordChannelRoles
                        .any { neededRole ->
                            neededRole in safeGuild.withStrategy(EntitySupplyStrategy.rest)
                                .getMemberOrNull(privateChannel!!.owner)!!.roleIds
                        }
                ) {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel!!.copy(
                            password = arguments.password
                        )
                    )
                    respond {
                        content = translateString("commands.channel.password.success", arguments.password)
                    }
                } else {
                    respond {
                        content = translateString("commands.channel.password.no-access")
                    }
                }
            } else {
                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel!!.copy(
                        password = null
                    )
                )
                respond {
                    content = translateString("commands.channel.password.remove.success")
                }
            }
        }

    }
}

class ChannelPasswordArguments : Arguments() {
    val password by optionalString("password", "The new password of the channel")
}
