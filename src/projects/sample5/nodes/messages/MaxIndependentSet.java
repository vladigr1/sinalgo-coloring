package projects.sample5.nodes.messages;


import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class MaxIndependentSet extends Message {
	public enum Request{
		INIT,
		ACTIVATE,
		DEACTIVATE
	}
	public Request req;
	public long num;
	public Node node;
	
	/**
	 * Default constructor. 
	 */
	public MaxIndependentSet(Request req, long num, Node node) {
		this.req = req;
		this.num = num;
		this.node = node;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}

}
