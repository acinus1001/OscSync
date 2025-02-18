package dev.kuro9.oscsync.osc

import com.illposed.osc.OSCBadDataEvent
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacketEvent
import com.illposed.osc.OSCPacketListener
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import dev.kuro9.oscsync.common.debugLog
import dev.kuro9.oscsync.common.errorLog
import dev.kuro9.oscsync.osc.model.VrcOscReceiveEvent
import dev.kuro9.oscsync.osc.model.VrcOscSendEvent
import jakarta.annotation.PostConstruct
import org.apache.tomcat.jni.Buffer.address
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
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

    fun sendPacket(address: String, data: Any) {
        oscTx.send(OSCMessage(address, listOf(data)))
    }

    @[Async EventListener]
    fun sendPacket(sendEvent: VrcOscSendEvent<*>) {
        sendPacket(sendEvent.address, sendEvent.payload)
    }

    override fun handlePacket(event: OSCPacketEvent) {
        val vrcEvent = with(event.packet as OSCMessage) {
            when (info.argumentTypeTags) {
                "T" -> VrcOscReceiveEvent.BoolType(
                    address = address,
                    value = true,
                )
                "F" -> VrcOscReceiveEvent.BoolType(
                    address = address,
                    value = false,
                )
                "i" -> VrcOscReceiveEvent.IntType(
                    address = address,
                    value = arguments.first() as Int
                )
                "f" -> VrcOscReceiveEvent.FloatType(
                    address = address,
                    value = arguments.first() as Float,
                )
                else -> VrcOscReceiveEvent.UnknownType(
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