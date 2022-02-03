package net.stckoverflw.privatechannel.command.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.Category
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.util.translateString

suspend fun EphemeralSlashCommand<*>.privateChannelCategoryCommand() =
    ephemeralSubCommand(::PrivateChannelCategoryArguments) {
        name = "category"
        description = "Set the category the private channels should be created in"
        guildAdminOnly()

        action {
            val guildSettings = PrivateChannelDatabase.getSettingsForGuild(safeGuild.id)

            PrivateChannelDatabase.guildSettingsCollection.save(
                guildSettings.copy(
                    privateChannelCategory = listOf(arguments.category.id)
                )
            )

            respond {
                content =
                    translateString("commands.settings.category.set", arguments.category.asChannelOf<Category>().name)
            }
        }
    }

class PrivateChannelCategoryArguments : Arguments() {
    val category by channel {
        name = "category"
        description = "The category the private channels should be created in"
        validate {
            failIf(value.type != ChannelType.GuildCategory, translate("commands.error.invalid_channel.category"))
        }
    }
}
