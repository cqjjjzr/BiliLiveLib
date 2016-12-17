package charlie.bililivelib.datamodel;

import charlie.bililivelib.i18n.I18n;

public enum UserGuardLevel {
    DEFAULT, GUARD, MASTER;

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
