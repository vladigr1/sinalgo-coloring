package projects.sample5.nodes.timers;

import projects.sample5.nodes.messages.PayloadMsg;
import projects.sample5.nodes.nodeImplementations.MyNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to initially send a message. This timer
 * is used in synchronous simulation mode to handle user
 * input while the simulation is not running. 
 */
public class MyTimer extends Timer {
	Message msg = null; // the msg to send

	/**
	 * @param msg The message to send
	 */
	public MyTimer(Message msg) {
		this.msg = msg;
	}
	
	@Override
	public void fire() {
		((MyNode) node).GMsg_handle(msg);
	}

}
