package projects.sample5.nodes.messages;


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
	public int num;
	public int id;
	
	/**
	 * Default constructor. 
	 */
	public MaxU(Request req, int num, int id) {
		this.req = req;
		this.num = num;
		this.id = id;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}

}
