package segment

import it.unibo.chain.segment7.LedSegment
import java.awt.GridLayout
import javax.swing.JFrame
import javax.swing.WindowConstants


class SegmentLed(name: String, width: Int = 60, height: Int = 30, x: Int = 0, y: Int = 0) :
    LedSegment(name, width, height) {
    private val frame: JFrame = JFrame()

    init {

        frame.name = name
        frame.setSize(width * 6, height * 10)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.setLocation(x, y)

        frame.layout = GridLayout(1, 1)

        frame.add(this)
        frame.isVisible = true
    }

    override fun setLedRep() {
        ledRep.addPoint(x + 10, y + 8)
        ledRep.addPoint(x + 100, y + 8)
        ledRep.addPoint(x + 110, y + 15)
        ledRep.addPoint(x + 100, y + 22)
        ledRep.addPoint(x + 10, y + 22)
        ledRep.addPoint(x + 2, y + 15)
    }
}
