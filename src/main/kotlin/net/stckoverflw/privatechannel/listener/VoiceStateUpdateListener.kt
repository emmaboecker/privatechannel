package net.stckoverflw.privatechannel.listener

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.createCategory
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.rest.builder.channel.PermissionOverwritesBuilder
import dev.kord.rest.builder.channel.addMemberOverwrite
import dev.schlaubi.stdx.coroutines.localSuspendLazy
import kotlinx.coroutines.flow.count
import net.stckoverflw.privatechannel.ChannelType
import net.stckoverflw.privatechannel.PrivateChannel
import net.stckoverflw.privatechannel.PrivateChannelDatabase
import net.stckoverflw.privatechannel.PrivateChannelEventModule
import net.stckoverflw.privatechannel.getSettingsForGuild
import net.stckoverflw.privatechannel.reFetch
import net.stckoverflw.privatechannel.util.getAllowedPermissionsForMember
import org.litote.kmongo.and
import org.litote.kmongo.eq

suspend fun PrivateChannelEventModule.voiceStateUpdateListener() = event<VoiceStateUpdateEvent> {
    action {
        if (event.state.channelId == event.old?.channelId) return@action // ignore mutes/deafn
        val channelId = event.state.channelId ?: event.old?.channelId ?: return@action
        val guildSettings = PrivateChannelDatabase.getSettingsForGuild(event.state.guildId)
        val guild = event.state.getGuild()
        val member = event.state.getMember()
        val privateChannel = PrivateChannelDatabase.privateChannelCollection.findOneById(channelId)
        val textChannel = localSuspendLazy { guild.getChannelOf<TextChannel>(privateChannel!!.textChannelId) }
        if (event.state.channelId != event.old?.channelId) {
            if (guildSettings.createChannel == event.state.channelId) {
                if (privateChannel == null) {
                    var category = guild.getChannelOfOrNull<Category>(guildSettings.privateChannelCategory?.first() ?: return@action) ?: return@action

                    if (category.channels.count() > 48) {
                        if (guildSettings.privateChannelCategory.size > 1) {
                            guildSettings.privateChannelCategory.subList(
                                1,
                                guildSettings.privateChannelCategory.lastIndex
                            ).forEachIndexed { index, snowflake ->
                                category = guild.getChannelOf(snowflake)
                                if (category.channels.count() > 48) {
                                    if (guildSettings.privateChannelCategory.getOrNull(index + 1) == null) {
                                        category = guild.createCategory(category.name) {
                                            position = category.rawPosition + 1
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val emptyChannel = PrivateChannel(
                        voiceChannelId = Snowflake(1),
                        textChannelId = Snowflake(1),
                        guild = guild.id,
                        owner = member.id,
                        voiceName = "<placeholder>",
                        textName = "<placeholder>",
                    )

                    fun PermissionOverwritesBuilder.disallowForEveryone(hideChannel: Boolean) {
                        addOverwrite(
                            Overwrite(
                                guild.everyoneRole.id,
                                OverwriteType.Role,
                                deny = Permissions {
                                    +Permission.Connect
                                    +Permission.Speak
                                    +Permission.SendMessages
                                    if (hideChannel) {
                                        +Permission.ViewChannel
                                    }
                                },
                                allow = Permissions()
                            )
                        )
                    }

                    fun PermissionOverwritesBuilder.addOwnerPermissions() {
                        addMemberOverwrite(member.id) {
                            allowed = emptyChannel.getAllowedPermissionsForMember(member.id)
                        }
                    }

                    val createdVoiceChannel = category.createVoiceChannel(member.displayName) {
                        addOwnerPermissions()
                        disallowForEveryone(false)
                    }
                    val createdTextChannel = category.createTextChannel(member.displayName) {
                        addOwnerPermissions()
                        disallowForEveryone(true)
                    }

                    val channel = emptyChannel.copy(
                        voiceChannelId = createdVoiceChannel.id, textChannelId = createdTextChannel.id,
                        voiceName = createdVoiceChannel.name,
                        textName = createdTextChannel.name,
                    )
                    PrivateChannelDatabase.privateChannelCollection.save(channel)

                    member.edit {
                        this.voiceChannelId = channel.voiceChannelId
                    }

                    return@action
                } else {
                    member.edit {
                        this.voiceChannelId = privateChannel.voiceChannelId
                    }
                }
            }
            if (privateChannel != null && member.id != privateChannel.owner) {
                if (member.id !in privateChannel.allowedUsers) {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        privateChannel.copy(
                            users = privateChannel.users + member.id
                        )
                    )
                }
                textChannel().addOverwrite(
                    PermissionOverwrite.forMember(
                        memberId = member.id,
                        allowed = privateChannel.reFetch().getAllowedPermissionsForMember(member.id)
                    ),
                    reason = "in channel"
                )
            }
        }
        if (event.state.channelId != event.old?.channelId && event.old?.channelId != guildSettings.createChannel) {
            val channel = PrivateChannelDatabase.privateChannelCollection.findOneById(event.old?.channelId ?: return@action) ?: return@action
            val discordVoiceChannel = guild.getChannelOfOrNull<VoiceChannel>(channel.voiceChannelId) ?: return@action
            val discordTextChannel = guild.getChannelOfOrNull<TextChannel>(channel.textChannelId) ?: return@action
            if (discordVoiceChannel.voiceStates.count() == 0) {
                if (channel.type == ChannelType.TEMPORARY) {
                    PrivateChannelDatabase.privateChannelCollection
                        .deleteOne(and(PrivateChannel::guild eq guild.id, PrivateChannel::owner eq member.id))
                    discordVoiceChannel.delete("Temporary Channel")
                    discordTextChannel.delete("Temporary Channel")
                }
            }
            if (event.old?.channelId == channel.voiceChannelId) {
                if (member.id !in channel.moderators && member.id != privateChannel?.owner) {
                    PrivateChannelDatabase.privateChannelCollection.save(
                        channel.copy(
                            users = channel.users - member.id
                        )
                    )
                    textChannel().addOverwrite(
                        PermissionOverwrite.forMember(
                            memberId = member.id,
                            denied = Permissions(Permission.All)
                        ),
                        reason = "not in channel anymore"
                    )
                }
            }
        }
    }
}
