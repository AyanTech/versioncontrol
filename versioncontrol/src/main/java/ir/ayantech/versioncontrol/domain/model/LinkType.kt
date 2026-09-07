package ir.ayantech.versioncontrol.domain.model

enum class LinkType(val rawValue: String) {
    DIRECT("direct"),
    PAGE("page");

    companion object {
        fun fromRawValue(value: String?): LinkType {
            return entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) } ?: PAGE
        }
    }
}
