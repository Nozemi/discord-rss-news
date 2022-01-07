# Discord News
An application that is designed to pull news from newspapers, blogs etc. The intention for this project is to provide a way to publish news articles related to tech in Discord channels, through [Discord's webhooks][discord-webhooks-intro].

This project is written in [Kotlin][kotlinlang]. I've also decided to use [Spring][spring] because I'm familiar with their dependency injection, and they provide some useful features to run HTTP requests etc.

## Features
- Configure RSS feeds to pull articles from, and have them sent to a Discord webhook.
  - Supports mapping values, so you can still add RSS feeds that doesn't match the default format.
- Uses Coroutines to delay messages until rate limit resets, so will never get to a rate limit.
- Uses Coroutines to pull from RSS feeds as well, so pulling from feeds can be done while still waiting to send messages.
- Message queue which is populated by the RSS feed monitor process, and then processed separately.

[spring]: https://spring.io/
[kotlinlang]: https://kotlinlang.org/
[discord-webhooks-intro]: https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks