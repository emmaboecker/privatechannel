package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.types.respond
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.channelModCommand() = ephemeralSubCommand(::ChannelModArguments) {
    name = "mod"
    description = "Add or remove a user as a moderator of this channel"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)
        if (hasAccessToPrivateChannel(privateChannel)) {
            if (!privateChannel!!.moderators.contains(arguments.member.id)) {
                if (privateChannel.moderators.contains(user.id) && privateChannel.owner != user.id) {
                    respond {
                        content = translateString("commands.channel.user.mod.fail.user_moderator")
                    }
                    return@action
                }
                if (privateChannel.owner == arguments.member.id) {
                    respond {
                        content = translateString("commands.channel.user.mod.fail.owner")
                    }
                    return@action
                }
                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        moderators = privateChannel.moderators + arguments.member.id
                    )
                )
                respond {
                    content = translateString("commands.channel.user.mod.success", arguments.member.mention)
                }
            } else {
                if (privateChannel.moderators.contains(user.id) && privateChannel.owner != user.id) {
                    respond {
                        content = translateString("commands.channel.user.unmod.fail.user_moderator")
                    }
                    return@action
                }
                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        moderators = privateChannel.moderators - arguments.member.id
                    )
                )
                respond {
                    content = translateString("commands.channel.user.unmod.success", arguments.member.mention)
                }
            }
        }
    }
}

class ChannelModArguments : Arguments() {
    val member by member {
        name = "user"
        description = "The User you want to add or remove as a moderator of this channel"
    }
}
