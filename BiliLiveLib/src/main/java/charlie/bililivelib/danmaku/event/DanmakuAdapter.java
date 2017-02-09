package charlie.bililivelib.danmaku.event;

import org.jetbrains.annotations.NotNull;

/**
 * 对{@link DanmakuListener}中所有方法给出的空实现，用于方便监听特定事件。
 *
 * @author Charlie Jiang
 * @see DanmakuListener
 * @since rv1
 */
public class DanmakuAdapter implements DanmakuListener {
    @Override
    public void danmakuEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void watcherCountEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void errorEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void startStopEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void statusEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void welcomeVipEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void giveGiftEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void globalGiftEvent(@NotNull DanmakuEvent event) {

    }

    @Override
    public void globalAnnounceEvent(@NotNull DanmakuEvent event) {

    }
}
