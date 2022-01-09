package net.stckoverflw.privatechannel.command.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.AllowedMentionType
import dev.kord.rest.builder.message.create.allowedMentions
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.translateString

suspend fun EphemeralSlashCommand<*>.passwordChannelRolesCommand() =
    ephemeralSubCommand(::PasswordChannelRolesArguments) {
        name = "password-channel-roles"
        description = "Add a Role that can make password protected channel (if no roles selected, everyone can)"
        guildAdminOnly()

        action {
            val guildSettings = PrivateChannelDatabase.getSettingsForGuild(safeGuild.id)

            if (!guildSettings.passwordChannelRoles.contains(arguments.role.id)) {
                PrivateChannelDatabase.guildSettingsCollection.save(
                    guildSettings.copy(
                        passwordChannelRoles = guildSettings.passwordChannelRoles + arguments.role.id
                    )
                )

                respond {
                    content = translateString("commands.settings.passwordchannel.role.added", arguments.role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            } else {
                PrivateChannelDatabase.guildSettingsCollection.save(
                    guildSettings.copy(
                        passwordChannelRoles = guildSettings.passwordChannelRoles - arguments.role.id
                    )
                )

                respond {
                    content = translateString("commands.settings.passwordchannel.role.removed", arguments.role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            }
        }
    }

class PasswordChannelRolesArguments : Arguments() {
    val role by role(
        "role",
        "The role to add to allowed password protected channel roles (if already, it'll be disallowed)"
    )
}
