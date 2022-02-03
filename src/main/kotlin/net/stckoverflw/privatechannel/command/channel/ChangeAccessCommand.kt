package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.any
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.suspendLazy
import net.stckoverflw.privatechannel.ChannelAccess
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.changeAccessCommand() = ephemeralSubCommand(::ChangeAccessArguments) {
    name = "access"
    description = "Change how the channel should be visible and accessible"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)
        if (hasAccessToPrivateChannel(privateChannel)) {
            val guildSettings = suspendLazy { PrivateChannelDatabase.getSettingsForGuild(safeGuild.id) }
            val guild = suspendLazy { safeGuild.asGuild() }
            val voiceChannel = safeGuild.getChannelOf<VoiceChannel>(privateChannel!!.voiceChannelId)
            when (ChannelAccess.valueOf(arguments.access)) {
                ChannelAccess.EVERYONE -> {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel.copy(
                            access = ChannelAccess.EVERYONE
                        )
                    )
                    voiceChannel.addOverwrite(
                        PermissionOverwrite.forEveryone(
                            safeGuild.id,
                            allowed = Permissions(Permission.ViewChannel, Permission.Connect)
                        ),
                        reason = "channel was made public"
                    )
                }
                ChannelAccess.PRIVATE -> {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel.copy(
                            access = ChannelAccess.PRIVATE
                        )
                    )
                    voiceChannel.addOverwrite(
                        PermissionOverwrite.forEveryone(
                            safeGuild.id,
                            allowed = Permissions(Permission.ViewChannel),
                            denied = Permissions(Permission.Connect)
                        ),
                        reason = "channel was made private"
                    )
                }
                ChannelAccess.INVISIBLE -> {
                    if (
                        guildSettings().invisibleChannelRoles.isEmpty() ||
                        guildSettings().invisibleChannelRoles
                            .any { neededRole ->
                                guild().getMemberOrNull(privateChannel.owner)?.roles?.let {
                                    it.any { role ->
                                        role.id == neededRole
                                    } 
                                } == true
                            }
                    ) {
                        PrivateChannelDatabase.privateChannelCollection.save(
                            privateChannel.copy(
                                access = ChannelAccess.INVISIBLE
                            )
                        )
                        voiceChannel.addOverwrite(
                            PermissionOverwrite.forEveryone(
                                safeGuild.id,
                                denied = Permissions(Permission.All)
                            ),
                            reason = "channel was made invisible"
                        )
                    } else {
                        respond {
                            content = translateString("commands.channel.change-access.no-access")
                        }
                        return@action
                    }
                }
            }
            respond {
                content = translateString("commands.channel.change-access.success", arguments.access.lowercase())
            }
        }
    }
}

class ChangeAccessArguments : Arguments() {
    val access by stringChoice {
        name = "access"
        description = "The new access level of this channel"
        choices = mutableMapOf(
            "Public" to ChannelAccess.EVERYONE.name,
            "Private" to ChannelAccess.PRIVATE.name,
            "Invisible (Private)" to ChannelAccess.INVISIBLE.name
        )
    }
}
