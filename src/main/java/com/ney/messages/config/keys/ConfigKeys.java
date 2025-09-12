package com.ney.messages.config.keys;

public final class ConfigKeys {

    private ConfigKeys() {}

    public static class Death {

        public static final String ENABLED = "death.enabled";

        public static class Message {
            public static final String ENABLED = "death.message.enabled";
            public static final String FORMAT = "death.message.format";
        }

        public static class Title {
            public static final String ENABLED = "death.title.enabled";
            public static final String TITLE = "death.title.title";
            public static final String SUBTITLE = "death.title.subtitle";
            public static final String FADE_IN = "death.title.fadeIn";
            public static final String STAY = "death.title.stay";
            public static final String FADE_OUT = "death.title.fadeOut";
        }

        public static class Sound {
            public static final String ENABLED = "death.sound.enabled";
            public static final String NAME = "death.sound.name";
            public static final String VOLUME = "death.sound.volume";
            public static final String PITCH = "death.sound.pitch";
        }

        public static class BossBar {
            public static final String ENABLED = "death.bossbar.enabled";
            public static final String TEXT = "death.bossbar.text";
            public static final String COLOR = "death.bossbar.color";
            public static final String STYLE = "death.bossbar.style";
            public static final String DURATION = "death.bossbar.duration";
            public static final String PROGRESS_DECAY = "death.bossbar.progress-decay";
        }

    }

    public static class Join {

        public static final String ENABLED = "join.enabled";

        public static class Message {
            public static final String ENABLED = "join.message.enabled";
            public static final String FORMAT = "join.message.format";
        }

        public static class Title {
            public static final String ENABLED = "join.title.enabled";
            public static final String TITLE = "join.title.title";
            public static final String SUBTITLE = "join.title.subtitle";
            public static final String FADE_IN = "join.title.fadeIn";
            public static final String STAY = "join.title.stay";
            public static final String FADE_OUT = "join.title.fadeOut";
        }

        public static class Sound {
            public static final String ENABLED = "join.sound.enabled";
            public static final String NAME = "join.sound.name";
            public static final String VOLUME = "join.sound.volume";
            public static final String PITCH = "join.sound.pitch";
        }

        public static class BossBar {
            public static final String ENABLED = "join.bossbar.enabled";
            public static final String TEXT = "join.bossbar.text";
            public static final String COLOR = "join.bossbar.color";
            public static final String STYLE = "join.bossbar.style";
            public static final String DURATION = "join.bossbar.duration";
            public static final String PROGRESS_DECAY = "join.bossbar.progress-decay";
        }

    }

    public static class Quit {

        public static final String ENABLED = "quit.enabled";

        public static class Message {
            public static final String ENABLED = "quit.message.enabled";
            public static final String FORMAT = "quit.message.format";
        }

        public static class Title {
            public static final String ENABLED = "quit.title.enabled";
            public static final String TITLE = "quit.title.title";
            public static final String SUBTITLE = "quit.title.subtitle";
            public static final String FADE_IN = "quit.title.fadeIn";
            public static final String STAY = "quit.title.stay";
            public static final String FADE_OUT = "quit.title.fadeOut";
        }

        public static class Sound {
            public static final String ENABLED = "quit.sound.enabled";
            public static final String NAME = "quit.sound.name";
            public static final String VOLUME = "quit.sound.volume";
            public static final String PITCH = "quit.sound.pitch";
        }

        public static class BossBar {
            public static final String ENABLED = "quit.bossbar.enabled";
            public static final String TEXT = "quit.bossbar.text";
            public static final String COLOR = "quit.bossbar.color";
            public static final String STYLE = "quit.bossbar.style";
            public static final String DURATION = "quit.bossbar.duration";
            public static final String PROGRESS_DECAY = "quit.bossbar.progress-decay";
        }

    }

    public static class Announcements {

        public static final String ENABLED = "announces.enabled";
        public static final String INTERVAL = "announces.interval";
        public static final String RANDOM = "announces.random";
        public static final String MESSAGES = "announces.messages";

        public static class Message {
            public static final String ENABLED = "message.enabled";
            public static final String FORMAT = "message.format";
            public static final String HOVER = "message.hover";
            public static final String CLICK = "message.click";
            public static final String CLICK_ACTION = "message.clickAction";
        }

        public static class Title {
            public static final String ENABLED = "title.enabled";
            public static final String TITLE = "title.title";
            public static final String SUBTITLE = "title.subtitle";
            public static final String FADE_IN = "title.fadeIn";
            public static final String STAY = "title.stay";
            public static final String FADE_OUT = "title.fadeOut";
        }

        public static class Sound {
            public static final String ENABLED = "sound.enabled";
            public static final String NAME = "sound.name";
            public static final String VOLUME = "sound.volume";
            public static final String PITCH = "sound.pitch";
        }

        public static class BossBar {
            public static final String ENABLED = "bossbar.enabled";
            public static final String TEXT = "bossbar.text";
            public static final String COLOR = "bossbar.color";
            public static final String STYLE = "bossbar.style";
            public static final String DURATION = "bossbar.duration";
            public static final String PROGRESS_DECAY = "bossbar.progress-decay";
        }

    }
}