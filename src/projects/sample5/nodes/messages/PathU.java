package projects.sample5.nodes.messages;

import projects.sample5.nodes.messages.MaxU.Request;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class PathU extends Message {
	public Node UNode;
	public Node parent;
	public double length;
	
	/**
	 * Default constructor. 
	 */
	public PathU(Node UNode, Node parent, double length) {
		this.UNode = UNode;
		this.parent = parent;
		this.length = length;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}

}
