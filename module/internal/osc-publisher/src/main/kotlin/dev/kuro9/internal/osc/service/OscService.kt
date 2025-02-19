package dev.kuro9.internal.osc.service

import com.illposed.osc.OSCBadDataEvent
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacketEvent
import com.illposed.osc.OSCPacketListener
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import dev.kuro9.common.util.errorLog
import dev.kuro9.common.util.infoLog
import dev.kuro9.internal.osc.model.VrcOscReceiveEvent
import dev.kuro9.internal.osc.model.VrcOscSendEvent
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.properties.Delegates

@Service
class OscService(
    private val eventPublisher: ApplicationEventPublisher
) {
    private var oscTx: OSCPortOut by Delegates.notNull()
    private var oscRx: OSCPortIn by Delegates.notNull()

    @PostConstruct
    fun init() {
        infoLog("Initializing OSC service")
        InetAddress.getLoopbackAddress()
        oscTx = OSCPortOut(InetSocketAddress(InetAddress.getLoopbackAddress(), 9001))
        oscRx = OSCPortIn(InetSocketAddress(InetAddress.getLoopbackAddress(), 9000))

        oscRx.addPacketListener(OscPacketListenerImpl())
        oscRx.startListening()
    }

    fun sendPacket(address: String, data: Any) {
        oscTx.send(OSCMessage(address, listOf(data)))
    }

    @[Async EventListener]
    fun sendPacket(sendEvent: VrcOscSendEvent<*>) {
        sendPacket(sendEvent.address, sendEvent.payload)
    }

    private inner class OscPacketListenerImpl : OSCPacketListener {
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
}