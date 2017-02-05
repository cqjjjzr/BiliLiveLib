package charlie.bilionlinekeeper.util

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.text.MessageFormat
import java.util.*

object I18n {
    private val BUNDLE_NAME = "BiliOnlineKeeper"
    private var strings: MutableMap<String, String> = HashMap()

    fun init() {
        strings.clear()
        val resourceBundle: ResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME)
        for (key: String in resourceBundle.keySet())
            strings[key] = resourceBundle.getString(key)
    }

    @Nls
    fun getString(@NonNls key: String): String? {
        return strings[key]
    }

    @Nls
    fun format(@NonNls key: String,
               @NonNls vararg arguments: Any): String? {
        return MessageFormat.format(getString(key), *arguments)
    }

    init {
        init()
    }
}