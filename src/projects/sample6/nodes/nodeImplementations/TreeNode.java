package projects.sample6.nodes.nodeImplementations;



import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

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
		
		if (ci >= 8) {// still not valid color
			update_ci();
			// forward the message to all children
			for(Edge e : outgoingConnections) {
				if(!e.endNode.equals(parent)) { // don't send it to the parent
					send(new MarkMessage(), e.endNode);
				}
			}
		}
		
		if (ci < 6) {
			this.setColor(lcolor[ci]);
		} else if (ci < 8) { // 6 < ci < 8
			shift_down();
		} else { // log the ci
			send(new MarkMessage(), this);
		}
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
				ci = bit_index*2 + ((ci >> bit_index) % 2);
				return;
			}
			++bit_index;
		}
	}
	
	private void shift_down() {
		
		int cp = parent.ci; // the root will never shift down because it set to 0
		int old_ci = ci;
		for (int i=0; i <6; ++i) {
			if ( i != cp) {
				ci = i;
				this.setColor(lcolor[ci]);
			}
		}
		
		// forward the message to all children
		for(Edge e : outgoingConnections) {
			if(!e.endNode.equals(parent)) { // don't send it to the parent
				if(e.endNode instanceof TreeNode) {
					TreeNode node = (TreeNode)(e.endNode);
					node.ci = old_ci;
					send(new MarkMessage(), node);
					node.setColor(Color.BLACK);
				}
			}
		}
	}
	@Override
	public void init() {
		this.setColor(Color.BLACK);
		this.ci = ID + 8; // enforce at least run once to sync with parent 
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
