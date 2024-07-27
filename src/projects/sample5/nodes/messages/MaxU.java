package projects.sample5.nodes.messages;


import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class MaxU extends Message {
	public enum Request{
		INIT,ACTIVE,DEACTIVE
	}
	public Request req;
	public double num;
	public Node node;
	
	/**
	 * Default constructor. 
	 */
	public MaxU(Request req, double num, Node node) {
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
