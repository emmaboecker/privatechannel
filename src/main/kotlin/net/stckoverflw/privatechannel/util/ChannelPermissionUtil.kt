package net.stckoverflw.privatechannel.util

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import net.stckoverflw.privatechannel.PrivateChannel

suspend fun EphemeralSlashCommandContext<*>.hasAccessToPrivateChannel(privateChannel: PrivateChannel?): Boolean {
    if (privateChannel == null) {
        respond {
            content = translateString("commands.channel.general.not-a-private-channel")
        }
        return false
    }
    if ((privateChannel.owner != member!!.id && !privateChannel.moderators.contains(member!!.id)) &&
        member?.asMember()?.hasPermission(Permission.ManageGuild) != true
    ) {
        respond {
            content = translateString("commands.channel.general.no-permission")
        }
        return false
    }
    return true
}