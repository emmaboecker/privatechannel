package net.stckoverflw.privatechannel.command.settings

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule

suspend fun SettingsModule.settingsCommand() = ephemeralSlashCommand {
    name = "settings"
    description = "<never used>"

    showSettingsCommand()
    setCreateChannelCommand()
    privateChannelCategoryCommand()
    passwordChannelRolesCommand()
    invisibleChannelRolesCommand()
    permanentChannelRolesCommand()
}