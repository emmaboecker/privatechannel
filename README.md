# Private Channel
A [Mikbot](https://github.com/DrSchlaubi/mikbot) Plugin which lets your Discord Memebers create their own Channel

## How to use

- <h3>For Members:</h3>
  - To create your own channel join the set category of the server (use `/settings show` if you don't know the channel)
  - `/channel user [user]`
    - add or remove a user from your channel
  - `/channel rename [text/voice] [string]`
    - renames the text or voice channel
  - `/channel mod [user]`
    - adds or removes a user as a moderator of the channel
  - `/channel ban [user]`
    - bans or unbans a user from the channel
  - `/channel password [password]`
    - sets the password of the channel (only usable if you have one of the from an admin set roles, if any)
  - `/channel type [temporary/permanent]`
    - sets the type of your channel (only settable to permanent if you have one of the from an admin set roles, if any)
  - `/channel access [public/private/invisible]`
    - set the access-level of the channel
      - `public` means everyone but banned users can join the channel
      - `private` means all users added manually or that joined using a set password
      - `invisible` means the same as private but the channel is not visible to normal users
  - `/channel join [owner] [password]`
    - used to join a channel of someone by using a password
  

- <h3>For Server Admins:</h3>
  - `/settings create-channel [voice-channel]` 
    - set the which users can join to create their own channel
  - `/settings category [category]`
    - set the category in wich the private channel will be created
  - `/settings permanent-channel-roles [role]`
    - add/remove a role with what, when users have it, they can make their channel permanent (if no roles are set, everyone will be able to)
  - `/settings invisible-channel-roles [role]`
    - add/remove a role with what, when users have it, they can make their channel invisible (if no roles are set, everyone will be able to)
  - `/settings password-channel-roles [role]`
    - add/remove a role with what, when users have it, they can make their channel password-protected (if no roles are set, everyone will be able to)
  - `/settings show`
    - shows the server's private channel settings