package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.stdx.coroutines.localSuspendLazy
import kotlinx.coroutines.flow.firstOrNull
import net.stckoverflw.privatechannel.ChannelAccess
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.channelBanCommand() = ephemeralSubCommand(::ChannelBanArguments) {
    name = "ban"
    description = "Ban or Unban a User"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)
        if (hasAccessToPrivateChannel(privateChannel)) {
            val voiceChannel =
                localSuspendLazy { safeGuild.getChannelOf<VoiceChannel>(privateChannel!!.voiceChannelId) }
            val textChannel =
                localSuspendLazy { safeGuild.getChannelOf<TextChannel>(privateChannel!!.textChannelId) }

            if (privateChannel!!.owner == arguments.member.id) {
                respond {
                    content = translateString("commands.channel.user.ban.fail.owner")
                }
                return@action
            }
            if (privateChannel.moderators.contains(user.id) && privateChannel.moderators.contains(arguments.member.id)) {
                respond {
                    content = translateString("commands.channel.user.ban.fail.other_moderator")
                }
                return@action
            }
            if (!privateChannel.bannedUsers.contains(arguments.member.id)) {
                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        users = privateChannel.users - arguments.member.id,
                        moderators = privateChannel.moderators - arguments.member.id,
                        bannedUsers = privateChannel.bannedUsers + arguments.member.id,
                    )
                )
                if (voiceChannel().voiceStates.firstOrNull { it.userId == arguments.member.id && it.channelId == voiceChannel().id } != null) {
                    arguments.member.edit {
                        voiceChannelId = null
                    }
                }
                voiceChannel().addOverwrite(
                    PermissionOverwrite.forMember(
                        memberId = arguments.member.id,
                        allowed = Permissions(),
                        denied = Permissions(Permission.All)
                    ),
                    reason = "banned from channel"
                )
                textChannel().addOverwrite(
                    PermissionOverwrite.forMember(
                        memberId = arguments.member.id,
                        allowed = Permissions(),
                        denied = Permissions(Permission.All)
                    ),
                    reason = "banned from channel"
                )
                respond {
                    content = translateString("commands.channel.user.ban.success", arguments.member.mention)
                }
            } else {
                PrivateChannelDatabase.privateChannelCollection.save(
                    privateChannel.copy(
                        bannedUsers = privateChannel.bannedUsers - arguments.member.id,
                    )
                )

                if (privateChannel.access != ChannelAccess.INVISIBLE) {
                    voiceChannel().addOverwrite(
                        PermissionOverwrite.forMember(
                            memberId = arguments.member.id,
                            allowed = Permissions(Permission.ViewChannel),
                            denied = Permissions()
                        ),
                        reason = "unbanned from channel"
                    )
                }

                respond {
                    content = translateString("commands.channel.user.unban.success", arguments.member.mention)
                }
            }
        }
    }
}

class ChannelBanArguments : Arguments() {
    val member by member {
        name = "member"
        description = "the user to ban or unban"
    }
}
