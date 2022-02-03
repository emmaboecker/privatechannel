package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.suspendLazy
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.getAllowedPermissionsForMember
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.joinChannelWithPasswordCommand() = ephemeralSubCommand(::JoinWithPasswordArguments) {
    name = "join"
    description = "Join a channel of someone with a password"
    check {
        anyGuild()
    }

    action {
        val joiningChannel = PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::owner eq arguments.channelOwner.id)
        val guildSettings = suspendLazy { PrivateChannelDatabase.getSettingsForGuild(safeGuild.id) }

        if (!(joiningChannel == null || member!!.id in joiningChannel.bannedUsers)) {
            if (member!!.id in joiningChannel.users || member!!.id in joiningChannel.moderators) {
                respond {
                    content = translateString("commands.channel.join.already")
                }
                return@action
            }
            if (member!!.id == joiningChannel.owner) {
                respond {
                    content = translateString("commands.channel.join.owner")
                }
                return@action
            }
            if (!(
                guildSettings().passwordChannelRoles.isEmpty() ||
                    guildSettings().passwordChannelRoles
                        .any { neededRole ->
                            neededRole in safeGuild.withStrategy(EntitySupplyStrategy.rest)
                                .getMemberOrNull(joiningChannel.owner)!!.roleIds
                        }
                )
            ) {
                PrivateChannelDatabase.privateChannelCollection.save(
                    joiningChannel.copy(
                        password = null
                    )
                )
            }
            if (arguments.password == joiningChannel.password) {
                val channel = joiningChannel.copy(
                    users = joiningChannel.users + member!!.id
                )
                PrivateChannelDatabase.privateChannelCollection.save(channel)

                safeGuild.getChannelOf<VoiceChannel>(joiningChannel.voiceChannelId).addOverwrite(
                    PermissionOverwrite.forMember(
                        memberId = member!!.id,
                        allowed = channel.getAllowedPermissionsForMember(member!!.id),
                    ),
                    reason = "user joined channel with password"
                )
                respond {
                    content = translateString("commands.channel.join.joined")
                }
            } else {
                respond {
                    content = translateString("commands.channel.join.wrong_password")
                }
            }
        } else {
            respond {
                content = translateString("commands.channel.join.no_channel_or_banned")
            }
        }
    }
}

class JoinWithPasswordArguments : Arguments() {
    val channelOwner by member {
        name = "owner"
        description = "The owner of the channel you want to join"
    }

    val password by string{
        name = "password"
        description = "The password of the channel"
    }
}
