package charlie.bililivelib.danmaku.event;

import org.jetbrains.annotations.NotNull;

public interface DanmakuListener {
    void danmakuEvent(@NotNull DanmakuEvent event);

    void watcherCountEvent(@NotNull DanmakuEvent event);

    void errorEvent(@NotNull DanmakuEvent event);

    void startStopEvent(@NotNull DanmakuEvent event);

    void statusEvent(@NotNull DanmakuEvent event);

    void welcomeVipEvent(@NotNull DanmakuEvent event);

    void giveGiftEvent(@NotNull DanmakuEvent event);

    void globalGiftEvent(@NotNull DanmakuEvent event);

    void globalAnnounceEvent(@NotNull DanmakuEvent event);
}
