package net.stckoverflw.privatechannel.util

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import net.stckoverflw.privatechannel.PrivateChannel

fun PrivateChannel.getAllowedPermissionsForMember(member: Snowflake) = Permissions {
    if (member == owner || member in moderators) {
        +Permission.ManageMessages
        +Permission.MoveMembers
        +Permission.CreatePrivateThreads
    }
    if (member in allowedUsers) {
        +Permission.ViewChannel
        // Voice Permission
        +Permission.Connect
        +Permission.Speak
        +Permission.Stream
        // Text Permission
        +Permission.SendMessages
        +Permission.ReadMessageHistory
        +Permission.AddReactions
        +Permission.AttachFiles
        +Permission.EmbedLinks
        +Permission.SendMessagesInThreads
        +Permission.CreatePublicThreads
    }
}
