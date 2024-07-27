package projects.sample5.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class DeliverMsg extends Message {
	public Node UNode;
	public Node to;
	
	/**
	 * Default constructor. 
	 */
	public DeliverMsg(Node to, Node UNode) {
		this.UNode = UNode;
		this.to = to;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}

}
