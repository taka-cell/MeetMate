package app.kobayashi.totti.meetmate

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CircleBackgroundDecorator(
    private val dates: Collection<CalendarDay>,
    colorHex: String
) : DayViewDecorator {

    private val drawable: GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.parseColor(colorHex))
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
    }
}
