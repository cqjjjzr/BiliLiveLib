package charlie.bililivelib.danmaku.event;

public interface DanmakuListener {
    void danmakuEvent(DanmakuEvent event);
    void watcherCountEvent(DanmakuEvent event);
    void errorEvent(DanmakuEvent event);
    void startStopEvent(DanmakuEvent event);
    void statusEvent(DanmakuEvent event);
    void welcomeVipEvent(DanmakuEvent event);
    void giveGiftEvent(DanmakuEvent event);
    void globalGiftEvent(DanmakuEvent event);
    void globalAnnounceEvent(DanmakuEvent event);
}
