package ca.ilianokokoro.umihi.music.core.helpers

object ComposeHelper {

    fun <T : Any> getLazyKey(element: T, id: String, index: Int): String {
        return "${element::class}_${id}_${index}"
    }
}
