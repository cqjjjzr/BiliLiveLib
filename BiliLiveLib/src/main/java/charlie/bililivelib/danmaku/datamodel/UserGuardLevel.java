package charlie.bililivelib.danmaku.datamodel;

import charlie.bililivelib.I18n;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * 标志一个用户的管理员等级。
 *
 * @author Charlie Jiang
 * @since rv1
 */
public enum UserGuardLevel {
    DEFAULT, GUARD, MASTER;

    @Nullable
    @Contract(pure = true)
    public static UserGuardLevel fromLevel(int level) {
        if (level < 0 || level > UserGuardLevel.values().length) {
            return null;
        }
        return UserGuardLevel.values()[level];
    }

    public String getDisplayString() {
        return I18n.getString("user.guard_level_" + this.name().toLowerCase());
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
