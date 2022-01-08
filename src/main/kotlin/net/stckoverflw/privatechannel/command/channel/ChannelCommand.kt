package net.stckoverflw.privatechannel.command.channel

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import net.stckoverflw.privatechannel.PrivateChannelCommandModule

suspend fun PrivateChannelCommandModule.channelCommand() = ephemeralSlashCommand {
    name = "channel"
    description = "<never used>"

    renameChannelCommand()
    channelUserCommand()
    channelModCommand()
    channelBanCommand()
    passwordCommand()
    changeTypeCommand()
    changeAccessCommand()
    joinChannelWithPasswordCommand()
}