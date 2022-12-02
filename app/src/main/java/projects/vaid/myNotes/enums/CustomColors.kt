package projects.vaid.myNotes.enums

enum class CustomColors(val color: String) {
    TRANSPARENT("#00000000"),
    RED("#EF9A9A"),
    Purple("#CE93D8"),
    LightBlue("#C5CAE9"),
    Green("#A5D6A7"),
    DarkGrey("#B0BEC5");

    //возвращяет список цветов
    companion object {
        fun customColorsAsList(): List<String> {
            val colors = mutableListOf<String>()
            values().forEach {
                colors.add(it.color)
            }
            return colors
        }
    }
}

