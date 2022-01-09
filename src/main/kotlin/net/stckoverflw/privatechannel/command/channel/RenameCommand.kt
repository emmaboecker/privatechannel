package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.channel.edit
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.util.hasAccessToPrivateChannel
import net.stckoverflw.privatechannel.util.translateString
import org.litote.kmongo.eq

suspend fun EphemeralSlashCommand<*>.renameChannelCommand() = ephemeralSubCommand(::RenameChannelArguments) {
    name = "rename"
    description = "Change the name of the Text/Voice-Channel"
    check {
        anyGuild()
    }

    action {
        val privateChannel =
            PrivateChannelDatabase.privateChannelCollection.findOne(PrivateChannel::textChannelId eq channel.id)
        if (hasAccessToPrivateChannel(privateChannel)) {
            when (arguments.channel.lowercase()) {
                "text" -> {
                    val newChannel = safeGuild.getChannelOfOrNull<TextChannel>(privateChannel!!.textChannelId)?.edit {
                        this.name = arguments.newName.lowercase()
                    }
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel.copy(
                            textName = newChannel!!.name
                        )
                    )
                }
                "voice" -> {
                    val newChannel = safeGuild.getChannelOfOrNull<VoiceChannel>(privateChannel!!.voiceChannelId)?.edit {
                        this.name = arguments.newName
                    }
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel.copy(
                            voiceName = newChannel!!.name
                        )
                    )
                }
            }
            respond {
                content = translateString("commands.channel.rename.success", arguments.newName)
            }
        }
    }
}

class RenameChannelArguments : Arguments() {
    val channel by stringChoice(
        "channel-type", "The type of channel you want to rename",
        mapOf(
            "Text" to "text",
            "Voice" to "voice"
        )
    )
    val newName by string("new-name", "The new name of your channel")
}
