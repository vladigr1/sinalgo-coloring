package projects.sample5.nodes.nodeImplementations;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Random;

import projects.defaultProject.models.messageTransmissionModels.ConstantTime;
import projects.sample5.nodes.messages.AckPayload;
import projects.sample5.nodes.messages.FloodFindMsg;
import projects.sample5.nodes.messages.MaxU;
import projects.sample5.nodes.messages.PathU;
import projects.sample5.nodes.messages.PayloadMsg;
import projects.sample5.nodes.messages.SendTo;
import projects.sample5.nodes.timers.GTimer;
import projects.sample5.nodes.timers.PayloadMessageTimer;
import projects.sample5.nodes.timers.RetryFloodingTimer;
import projects.sample5.nodes.timers.RetryPayloadMessageTimer;
import projects.sample6.nodes.messages.MarkMessage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

/**
 * A node that implements a flooding strategy to determine paths to other nodes.
 */
public class FNode extends Node {


//	/**
//	 * A routing table entry
//	 */
//	public class RoutingEntry {
//		public int sequenceNumber; // sequence number used when this entry was created
//		public int numHops; // number of hops to reach destination
//		public Node nextHop; // next hop to take  
//
//		public RoutingEntry(int seqNumber, int hops, Node hop) {
//			this.sequenceNumber = seqNumber;
//			this.numHops = hops;
//			this.nextHop = hop;
//		}
//	}
	
	// counter, incremented and added for each msg sent (not forwarded) by this node
	public int seqID = 0; // an ID used to distinguish successive msg
	
	// The routing table of this node, maps destination node to a routing entry
//	Hashtable<Node, RoutingEntry> routingTable = new Hashtable<Node, RoutingEntry>();
	
	// messages that could not be sent so far, because no route is known
	public static Vector<FNode> U = new Vector<FNode>();
	public static  Hashtable<Node, Integer> numOfSendedMessagesFromNode = new Hashtable<Node, Integer>();
	
//	/**
//	 * Method to clear this node's routing table 
//	 */
//	public void clearRoutingTable() {
//		routingTable.clear();
//	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// The message delivery time must be constant, this allows the project
		// to easily predict the waiting times
		if(!(Tools.getMessageTransmissionModel() instanceof ConstantTime)) {
			Tools.fatalError("This project requires that messages are sent with the ConstantTime MessageTransmissionModel.");
		}
	}
	
	
	public void GMsg_handle(Message msg) {
		sendDirect(msg, this);
	}
	
	public class ParentDistnace{
		public Node parent;
		public double length;
		
		public ParentDistnace( Node parent, double length) {
			this.parent = parent;
			this.length = length;
		}
		
	}
	
	// [UNode] -> parent_node
	private Color[] lcolor = {Color.PINK, Color.ORANGE, Color.GREEN, Color.MAGENTA, Color.BLUE, Color.CYAN}; //RED not in use
	Hashtable<Node, ParentDistnace> parentTable = new Hashtable<Node, ParentDistnace>();
	public void resetParentTable() {
		parentTable = new Hashtable<Node, ParentDistnace>();
	}
	
	public void handlePathU(PathU msg) {
		double distance = this.getPosition().squareDistanceTo(msg.parent.getPosition());
		
		ParentDistnace pd = parentTable.get(msg.UNode);
		double cur_length = msg.length + distance;
		if (pd == null || (cur_length < pd.length) ) {
			Integer currentMax = numOfSendedMessagesFromNode.get(msg.UNode);
			if(currentMax == null)
			{
				currentMax = 0;
			}
			Integer myNumIteration = msg.numIteration + 1;
			numOfSendedMessagesFromNode.put(msg.UNode, Math.max(currentMax,myNumIteration));
			// shorter length
			parentTable.put(msg.UNode, new ParentDistnace(msg.parent, cur_length));
			for(Edge e : outgoingConnections) {
				send(new PathU(msg.UNode, this, cur_length, myNumIteration), e.endNode);
				if(e.endNode == msg.parent) {
					e.endNode.setColor(lcolor[msg.UNode.ID % lcolor.length]);
				}
			}
		}
	}
	
	public void handleSendTo(SendTo msg) {
		if (this == msg.to) { return ;}
		
		if (this == msg.UNode) {
			for (Edge e : outgoingConnections) {
				if(e.endNode == msg.to) {
					send(msg, e.endNode);
				}
			}
		}
		
		send(msg, parentTable.get(msg.UNode).parent);
	}
	
	private int maxu_num;
	private boolean maxu_done;
	public Node deactivtorNode;
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			if(msg instanceof PathU) {
				handlePathU((PathU)msg);
			}
			if(msg instanceof SendTo) {
				handleSendTo((SendTo)msg);
			}
			if(msg instanceof MaxU) {
				MaxU maxu = (MaxU)msg;
				
				if (maxu.req == MaxU.Request.INIT) {
					maxu_num = maxu.num;
					maxu_done = false;
					FNode.U.clear();
					for(Edge e : outgoingConnections) {
						send(new MaxU(MaxU.Request.ACTIVE, maxu_num, this), e.endNode);
					}
					return;
				}
				
				if (maxu_done) {
					inbox.reset();
					return;
				}
				
				if (maxu.req == MaxU.Request.ACTIVE) {
					setColor(Color.GREEN);
					if(maxu.num > maxu_num) {
						inbox.reset();
						for(Edge e : outgoingConnections) {
							send(new MaxU(MaxU.Request.ACTIVE, maxu_num, this), e.endNode);
						}
						return;
					}
				} else if (maxu.req == MaxU.Request.DEACTIVE){
					if ( (maxu.num > maxu_num)  || (maxu.num == maxu_num) && maxu.node.ID > this.ID) {
						setColor(Color.RED);
						deactivtorNode =  maxu.node;
						FNode.U.remove(this);
						maxu_done = true;
						return;
					}
				}
				
				if ( !inbox.hasNext()) {
					// active and handle all neighbor messages => in U
					U.add(this);
					setColor(Color.BLUE);
					for(Edge e : outgoingConnections) {
						send(new MaxU(MaxU.Request.DEACTIVE, maxu_num, this), e.endNode);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#handleNAckMessages(sinalgo.nodes.messages.NackBox)
	 */
	public void handleNAckMessages(NackBox nackBox) {
		Logging log = Logging.getLogger();
		while(nackBox.hasNext()) {
			nackBox.next();
			log.logln("Node " + this.ID + " could not send a message to " + nackBox.getReceiver().ID);
		}
	}
	
	@NodePopupMethod(menuText = "Send Message To...")
	public void sendMessageTo() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null && !(n instanceof FNode) ) {
					return; // aborted
				}
				FNode to = (FNode)n;
				GTimer t = new GTimer(new SendTo(to, to.deactivtorNode));
				t.startRelative(1, FNode.this);
			}
		}, "Select a node to send a message to...");
	}

	/**
	 * Tries to send a message if there is a routing entry. 
	 * If there is no routing entry, a search is started, and the
	 * message is put in a buffer of messages on hold.
	 * @param msg
	 * @param to
	 */
//	public void sendPayloadMessage(PayloadMsg msg) {
//		RoutingEntry re = routingTable.get(msg.destination);
//		if(re != null) {
//			if(msg.sender.equals(this) && msg.requireACK) { // this node wants to have the message sent - it waits for an ack
//				RetryPayloadMessageTimer rpmt = new RetryPayloadMessageTimer(msg);
//				rpmt.startRelative(re.numHops * 3, this); // We wait a bit longer than necessary
//				if(msg.ackTimer != null){
//					msg.ackTimer.deactivate();
//				}
//				msg.ackTimer = rpmt;
//			}
//			send(msg, re.nextHop);
//			return ;
//		} else {
//			lookForNode(msg.destination, 4);
//			messagesOnHold.add(msg);
//		}
//	}
	
//	/**
//	 * Starts a search for a given node with the given TTL
//	 * @param destination
//	 * @param ttl
//	 */
//	public void lookForNode(Node destination, int ttl) {
//		if(ttl > 10000000) { // this limits to graphs of diameter 10^7 ....
//			return; // we've already searched too far - there is probably no connection! 
//		}
//
//		FloodFindMsg m = new FloodFindMsg(++this.seqID, this, destination);
//		m.ttl = ttl;
//		RetryFloodingTimer rft = new RetryFloodingTimer(destination, m.ttl);
//		// The TTL must depend on the message transmission time. We assume here a constant msg. transm. time of 1 unit.
//		rft.startRelative(m.ttl * 2 + 1, this); 
//		m.retryTimer = rft;
//		this.broadcast(m);
//	}
//	
//	private void useNewRoutingInfo(Node destination, Node nextHop) {
//		Iterator<PayloadMsg> it = messagesOnHold.iterator();
//		while(it.hasNext()) {
//			PayloadMsg m = it.next();
//			if(m.destination.equals(destination)) {
//				this.sendPayloadMessage(m);
//				it.remove();
//			}
//		}
//	}
	
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		// we could remove routing-table entries that use this neighbor
	}

	@Override
	public void preStep() {
	}

	@Override
	public void postStep() {
	}
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#toString()
	 */
	public String toString() {
		// show the routing table entries
//		String r = "";
//		for(Entry<Node, RoutingEntry> e : routingTable.entrySet()) {
//			r += e.getKey().ID + " => " + e.getValue().nextHop.ID + " (" + e.getValue().numHops+ ")"+ "\n";
//		}
		return "maxu_num: " + String.valueOf(maxu_num);
	}
}
