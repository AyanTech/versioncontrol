package ir.ayantech.versioncontrol.domain.model

enum class UpdateStatus(val rawValue: String) {
    NOT_REQUIRED("NotRequired"),
    OPTIONAL("Optional"),
    MANDATORY("Mandatory");

    companion object {
        fun fromRawValue(value: String?): UpdateStatus {
            return entries.firstOrNull { it.rawValue.equalsIgnoreCase(value) } ?: NOT_REQUIRED
        }

        private fun String.equalsIgnoreCase(other: String?): Boolean {
            return this.equals(other, ignoreCase = true)
        }
    }
}
