package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist.Companion.BROKER_MOLECULE
import it.unibo.alchemist.jakta.reactions.JaktaAgentForAlchemist
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.distributed.MessageBroker
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.alchemist.model.Environment as AlchemistEnvironment

class JaktaForAlchemistMessageBroker<P : Position<P>>(
    // For incoming messages
    private val jaktaDevice: JaktaEnvironmentForAlchemist<P>,
    private val messageBoxes: MutableMap<String, MutableList<Message>> = mutableMapOf(),
) : MessageBroker, NodeProperty<Any?> {

    override fun putInMessageBox(receiver: String, message: Message) {
        if (messageBoxes.contains(receiver)) {
            messageBoxes[receiver]!!.add(message)
        } else {
            messageBoxes += receiver to mutableListOf(message)
        }
    }

    override fun send(receiver: String, host: String, message: Message) {
        val mbox: JaktaForAlchemistMessageBroker<P> = jaktaDevice.alchemistEnvironment.getNodeByID(host.toInt()).asProperty()
        mbox.putInMessageBox(receiver, message)
    }

    fun send(receiverWithHost: String, message: Message) {
        val receiverAndHost = receiverWithHost.split("@")
        send(receiverAndHost.first(), receiverAndHost[1], message)
    }

    override fun broadcast(message: Message) {
        jaktaDevice.alchemistEnvironment.getNeighborhood(node).forEach {
            val mbox: JaktaForAlchemistMessageBroker<P> = it.asProperty()
            it.reactions.filterIsInstance<JaktaAgentForAlchemist<P>>().forEach { agentContainer ->
                mbox.putInMessageBox(agentContainer.agent.name, message)
            }
        }
    }

    override fun pop(receiver: String, host: String): Message? = messageBoxes[receiver]?.removeFirst()

    override fun nextMessage(receiver: String, host: String): Message? =
        messageBoxes[receiver]?.getOrNull(0)

    override val node: Node<Any?>
        get() = jaktaDevice.node

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> {
        TODO("Not yet implemented")
    }
}
