package ir.ayantech.versioncontrol.domain.model

data class UpdateInfo(
    val title: String? = null,
    val body: String? = null,
    val acceptButtonText: String? = null,
    val rejectButtonText: String? = null,
    val changeLogs: List<String> = emptyList(),
    val linkType: LinkType = LinkType.PAGE,
    val link: String? = null,
    val updateStatus: UpdateStatus = UpdateStatus.NOT_REQUIRED,
    val textToShare: String? = null
)
