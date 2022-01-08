package net.stckoverflw.privatechannel.command.settings

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.translateString

suspend fun EphemeralSlashCommand<*>.showSettingsCommand() = ephemeralSubCommand {
    name = "show"
    description = "Shows the Private Channel Settings for this Server"
    check {
        anyGuild()
    }

    action {
        val guildSettings = PrivateChannelDatabase.getSettingsForGuild(safeGuild.id)

        respond {
            embed {
                title = translateString("commands.settings.show.title", safeGuild.asGuild().name)
                field {
                    name = translateString("commands.settings.show.set-channel")

                    value = if (guildSettings.createChannel != null && safeGuild.getChannelOrNull(guildSettings.createChannel) != null) {
                        "<#${guildSettings.createChannel}>"
                    } else {
                        PrivateChannelDatabase.guildSettingsCollection.save(
                            guildSettings.copy(
                                createChannel = null
                            )
                        )
                        translateString("commands.settings.show.no-channel-set")
                    }
                }
                field {
                    name = translateString("commands.settings.show.permanent-channel")
                    value = if (guildSettings.permanentChannelRoles.isNotEmpty()) {
                        guildSettings.permanentChannelRoles
                            .mapNotNull { if (safeGuild.getRoleOrNull(it) != null) "<@&$it>" else null }
                            .joinToString(", ")
                    } else {
                        translateString("commands.settings.show.no-roles-set")
                    }
                }
                field {
                    name = translateString("commands.settings.show.invisible-channel")
                    value = if (guildSettings.invisibleChannelRoles.isNotEmpty()) {
                        guildSettings.invisibleChannelRoles
                            .mapNotNull { if (safeGuild.getRoleOrNull(it) != null) "<@&$it>" else null }
                            .joinToString(", ")
                    } else {
                        translateString("commands.settings.show.no-roles-set")
                    }
                }
                field {
                    name = translateString("commands.settings.show.password-channel")
                    value = if (guildSettings.passwordChannelRoles.isNotEmpty()) {
                        guildSettings.passwordChannelRoles
                            .mapNotNull { if (safeGuild.getRoleOrNull(it) != null) "<@&$it>" else null }
                            .joinToString(", ")
                    } else {
                        translateString("commands.settings.show.no-roles-set")
                    }
                }
            }
        }
    }
}