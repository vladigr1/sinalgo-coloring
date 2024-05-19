package projects.sample6.nodes.nodeImplementations;



import java.awt.Color;
import java.util.ArrayList;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.sample6.nodes.messages.MarkMessage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

/**
 * An internal node (or leaf node) of the tree.
 * Note that the leaves are instances of LeafNode, a subclass of this class. 
 */
public class TreeNode extends Node {

	public TreeNode parent = null; // the parent in the tree, null if this node is the root
	private Color[] lcolor = {Color.PINK, Color.ORANGE, Color.GREEN, Color.MAGENTA, Color.BLUE, Color.CYAN}; //RED not in use
	
	public int ci = ID;
	
	public String toString() {
		return "Node(ID=" + this.ID + ", ci=" + this.ci + ")"; 
	}
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
//		while(inbox.hasNext()) {
//			Message m = inbox.next();
//			if(m instanceof MarkMessage) {
//				if(parent == null || !inbox.getSender().equals(parent)) {
//					continue;// don't consider mark messages sent by children
//				}
		
		if (ci > 5) {// still not valid color
			System.out.println("ID, before_ci: " + ID +" , "+ ci);
			update_ci();
			System.out.println("ID, update_ci: " + ID +" , "+ ci);
			// forward the message to all children
			for(Edge e : outgoingConnections) {
				if(!e.endNode.equals(parent)) { // don't send it to the parent
					send(new MarkMessage(), e.endNode);
				}
			}
			// alternatively, we could broadcast the message:
			// broadcast(m);
		}
		
		if (ci < 6) {
			this.setColor(lcolor[ci]);
		}else{
			send(new MarkMessage(), this);
		}
		
//			}
//		}
	}

	private void update_ci() {
		if (parent == null) { // root
			ci = 0; 
			return;
		}
		int cp = parent.ci;
		int bit_index = 0;
		while(true) {
			if ((cp & (1 << bit_index)) != (ci & (1 << bit_index))) {
				System.out.println("bit_index=" + bit_index);
				ci = bit_index*2 + ((ci >> bit_index) % 2);
				return;
			}
			++bit_index;
		}
	}
	@Override
	public void init() {
		this.setColor(Color.BLACK);
		this.ci = this.ID;
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
	}

	@Override
	public void postStep() {
	}
	
	@NodePopupMethod(menuText = "Color children") 
	public void colorKids() {
		MarkMessage msg = new MarkMessage();
		MessageTimer timer = new MessageTimer(msg);
		timer.startRelative(1, this);
	}

}
