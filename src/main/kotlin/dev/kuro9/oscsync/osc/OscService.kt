package dev.kuro9.oscsync.osc

import com.illposed.osc.OSCBadDataEvent
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacketEvent
import com.illposed.osc.OSCPacketListener
import com.illposed.osc.transport.OSCPort
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
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

    fun init() {
        InetAddress.getLoopbackAddress()
        oscTx = OSCPortOut(InetSocketAddress(InetAddress.getLoopbackAddress(), 9001))
        oscRx = OSCPortIn(InetSocketAddress(InetAddress.getLoopbackAddress(), 9000))

        oscRx.addPacketListener(this)
        oscRx.startListening()
    }

    override fun handlePacket(event: OSCPacketEvent) {
        val vrcEvent = with(event.packet as OSCMessage) {
            when (this.info.argumentTypeTags) {
                "T" -> VrcOscEvent.Bool(
                    address = address,
                    value = this.arguments.first() == 'T'
                )
                else -> TODO()
            }
        }

        eventPublisher.publishEvent(vrcEvent)
    }

    override fun handleBadData(event: OSCBadDataEvent) {
        TODO("Not yet implemented")
    }
}