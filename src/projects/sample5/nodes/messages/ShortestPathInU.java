package projects.sample5.nodes.messages;

import projects.sample5.nodes.messages.MaxIndependentSet.Request;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class ShortestPathInU extends Message {
	public int numIteration;
	public Node UNode;
	public Node parent;
	public double length;
	
	/**
	 * Default constructor. 
	 */
	public ShortestPathInU(Node UNode, Node parent, double length, int numIteration) {
		this.numIteration = numIteration;
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
