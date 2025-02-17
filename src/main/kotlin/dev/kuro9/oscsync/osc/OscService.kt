package dev.kuro9.oscsync.osc

import com.illposed.osc.OSCBadDataEvent
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacketEvent
import com.illposed.osc.OSCPacketListener
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import dev.kuro9.oscsync.common.errorLog
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.net.*
import kotlin.properties.Delegates

@Service
class OscService(
    private val eventPublisher: ApplicationEventPublisher
) : OSCPacketListener {
    private var oscTx: OSCPortOut by Delegates.notNull()
    private var oscRx: OSCPortIn by Delegates.notNull()

    @PostConstruct
    fun init() {
        InetAddress.getLoopbackAddress()
        oscTx = OSCPortOut(InetSocketAddress(InetAddress.getLoopbackAddress(), 9001))
        oscRx = OSCPortIn(InetSocketAddress(InetAddress.getLoopbackAddress(), 9000))

        oscRx.addPacketListener(this)
        oscRx.startListening()
    }

    override fun handlePacket(event: OSCPacketEvent) {
        val vrcEvent = with(event.packet as OSCMessage) {
            when (info.argumentTypeTags) {
                "T" -> VrcOscEvent.BoolType(
                    address = address,
                    value = true,
                )
                "F" -> VrcOscEvent.BoolType(
                    address = address,
                    value = false,
                )
                "i" -> VrcOscEvent.IntType(
                    address = address,
                    value = arguments.first() as Int
                )
                "f" -> VrcOscEvent.FloatType(
                    address = address,
                    value = arguments.first() as Float,
                )
                else -> VrcOscEvent.UnknownType(
                    address = address,
                    value = arguments.toString()
                )
            }
        }

        eventPublisher.publishEvent(vrcEvent)
    }

    override fun handleBadData(event: OSCBadDataEvent) {
        errorLog("Osc Bad Data Received", event.exception)
    }
}