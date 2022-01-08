package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.any
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.ChannelAccess
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.reFetch
import net.stckoverflw.privatechannel.util.getAllowedPermissionsForMember
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.channelUserCommand() = ephemeralSubCommand(::ChannelUserArguments) {
    name = "user"
    description = "Add or remove a user from the channel"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)

        if (hasAccessToPrivateChannel(privateChannel)) {
            val voiceChannel = safeGuild.getChannelOf<VoiceChannel>(privateChannel!!.voiceChannelId)

            if (!privateChannel.users.contains(arguments.member.id)) {
                if (privateChannel.bannedUsers.contains(arguments.member.id)) {
                    respond {
                        content = translateString("commands.channel.user.add.fail.banned")
                    }
                    return@action
                }
                if (privateChannel.moderators.contains(arguments.member.id)) {
                    respond {
                        content = translateString("commands.channel.user.add.fail.already.moderator")
                    }
                    return@action
                }
                if (privateChannel.owner == arguments.member.id) {
                    respond {
                        content = translateString("commands.channel.user.add.fail.already.owner")
                    }
                    return@action
                }

                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        users = privateChannel.users + arguments.member.id
                    )
                )

                voiceChannel.addOverwrite(PermissionOverwrite.forMember(
                    memberId = arguments.member.id,
                    allowed = privateChannel.reFetch().getAllowedPermissionsForMember(arguments.member.id),
                ), reason = "user was added to the channel")

                respond {
                    content = translateString("commands.channel.user.add.success", arguments.member.mention)
                }
            } else {
                if (privateChannel.moderators.contains(user.id) && privateChannel.moderators.contains(arguments.member.id)) {
                    respond {
                        content = translateString("commands.channel.user.remove.fail.other_moderator")
                    }
                    return@action
                }
                if (privateChannel.owner == arguments.member.id) {
                    respond {
                        content = translateString("commands.channel.user.remove.fail.owner")
                    }
                    return@action
                }

                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        users = privateChannel.users - arguments.member.id,
                        moderators = privateChannel.moderators - arguments.member.id
                    )
                )


                if (privateChannel.access != ChannelAccess.EVERYONE) {
                    if (voiceChannel.voiceStates.any { it.userId == arguments.member.id && it.channelId == voiceChannel.id }) {
                        arguments.member.edit {
                            voiceChannelId = null
                        }
                    }
                    voiceChannel.addOverwrite(PermissionOverwrite.forMember(
                        memberId = arguments.member.id,
                        denied = privateChannel.reFetch().getAllowedPermissionsForMember(arguments.member.id),
                    ), reason = "user was removed from the channel")
                }


                respond {
                    content = translateString("commands.channel.user.remove.success", arguments.member.mention)
                }
            }
        }
    }
}

class ChannelUserArguments : Arguments() {
    val member by member("user", "The user you want to add or remove from this channel")
}
