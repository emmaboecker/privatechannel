package net.stckoverflw.privatechannel

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import net.stckoverflw.privatechannel.command.channel.channelCommand
import net.stckoverflw.privatechannel.command.settings.settingsCommand
import net.stckoverflw.privatechannel.listener.voiceStateUpdateListener
import org.pf4j.Extension
import com.kotlindiscord.kord.extensions.extensions.Extension as KordExtension

@PluginMain
class PrivateChannelPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::PrivateChannelCommandModule)
        add(::PrivateChannelEventModule)
    }

}

@Extension
class PrivateChannelSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        settingsCommand()
    }
}

class PrivateChannelEventModule : KordExtension() {
    override val name: String = "private channel event listener module"

    override suspend fun setup() {
        voiceStateUpdateListener()
    }

}

class PrivateChannelCommandModule : KordExtension() {
    override val name: String = "private channel command module"

    override suspend fun setup() {
        channelCommand()
    }

}
