# Voice of REALIST

Telegram bot for sending notifications. Bot support three users groups: admins, writers and readers. Reader can only
subscribe/unsubscribe to publications, writers can write and send publications, admins can add/remove other users to
groups.

## Configuration

Configuration file contains three parameters:

```ini
creator = -1
token = 1234567890:TOKEN
user_name = voice-of-realist
```

where `creator` - identifier of telegram user, this user will always be admin and writer. `token` - telegram bot token,
`user_name` - this name will be user as database name.
