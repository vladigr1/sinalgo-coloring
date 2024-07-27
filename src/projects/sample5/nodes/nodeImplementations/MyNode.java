package projects.sample5.nodes.nodeImplementations;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Vector;
import projects.defaultProject.models.messageTransmissionModels.ConstantTime;
import projects.sample5.nodes.messages.MaxU;
import projects.sample5.nodes.messages.PathU;
import projects.sample5.nodes.messages.SendTo;
import projects.sample5.nodes.timers.MyTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class MyNode extends Node {
	
	public static Vector<MyNode> U = new Vector<MyNode>();
	public static  Hashtable<Node, Integer> numOfSendedMessagesFromNode = new Hashtable<Node, Integer>();
	
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
	
	private Color[] lcolor = {Color.GREEN, Color.ORANGE, Color.PINK, Color.WHITE, Color.YELLOW, Color.BLUE}; //RED not in use
	Hashtable<Node, ParentDistnace> parentTable = new Hashtable<Node, ParentDistnace>();
	public void resetParentTable() {
		parentTable = new Hashtable<Node, ParentDistnace>();
	}
	
	public void handlePathU(PathU msg) {
		double distance = this.getPosition().distanceTo(msg.parent.getPosition());
		
		ParentDistnace pd = parentTable.get(msg.UNode);
		double cur_length = msg.length + distance;
		if (pd == null || (cur_length < pd.length) ) { // Check for shorter path
			Integer currentMax = numOfSendedMessagesFromNode.get(msg.UNode);
			if(currentMax == null)
			{
				currentMax = 0;
			}
			Integer myNumIteration = msg.numIteration + 1;
			numOfSendedMessagesFromNode.put(msg.UNode, Math.max(currentMax,myNumIteration));
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
	
	private long maxu_num;
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
					MyNode.U.clear();
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
						MyNode.U.remove(this);
						maxu_done = true;
						return;
					}
				}
				
				if ( !inbox.hasNext()) {
					// active and handle all neighbor messages => in U
					U.add(this);
					setColor(Color.BLUE);
					deactivtorNode = this;
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
		Global.systemState = 3;
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null && !(n instanceof MyNode) ) {
					return;
				}
				MyNode to = (MyNode)n;
				MyTimer t = new MyTimer(new SendTo(to, to.deactivtorNode));
				t.startRelative(1, MyNode.this);
			}
		}, "Select a node to send a message to...");
	}
		
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#toString()
	 */
	public String toString() {
		return "maxu_num: " + String.valueOf(maxu_num);
	}


	@Override
	public void preStep() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void postStep() {
		// TODO Auto-generated method stub
		
	}
}
