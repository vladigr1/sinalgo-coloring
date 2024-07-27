package projects.sample5.nodes.nodeImplementations;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Vector;
import projects.defaultProject.models.messageTransmissionModels.ConstantTime;
import projects.sample5.nodes.messages.MaxIndependentSet;
import projects.sample5.nodes.messages.ShortestPathInU;
import projects.sample5.nodes.messages.DeliverMsg;
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
	
	public void shortestPathInUHandler(ShortestPathInU message) {
		double distance = this.getPosition().distanceTo(message.parent.getPosition());
		
		ParentDistnace pd = parentTable.get(message.UNode);
		double cur_length = message.length + distance;
		if (pd == null || (cur_length < pd.length) ) { // Check for shorter path
			Integer currentMax = numOfSendedMessagesFromNode.get(message.UNode);
			if(currentMax == null)
			{
				currentMax = 0;
			}
			Integer myNumIteration = message.numIteration + 1;
			numOfSendedMessagesFromNode.put(message.UNode, Math.max(currentMax,myNumIteration));
			parentTable.put(message.UNode, new ParentDistnace(message.parent, cur_length));
			for(Edge e : outgoingConnections) {
				send(new ShortestPathInU(message.UNode, this, cur_length, myNumIteration), e.endNode);
				if(e.endNode == message.parent) {
					e.endNode.setColor(lcolor[message.UNode.ID % lcolor.length]);
				}
			}
		}
	}
	
	public void deliverMsgHandler(DeliverMsg message) {
		if (this == message.to) { return ;}
		
		if (this == message.UNode) {
			for (Edge e : outgoingConnections) {
				if(e.endNode == message.to) {
					send(message, e.endNode);
				}
			}
		}
		
		send(message, parentTable.get(message.UNode).parent);
	}
	
	public void maxIndependentSetHandler(MaxIndependentSet message)
	{	
		boolean treatmentDone = false;
		if (finished) {
			treatmentDone = true;
			inbox.reset();
		}
		
		else if (message.req == MaxIndependentSet.Request.INIT) {
			treatmentDone = true;
			handleInitRequest(message);
		}
		
		else if (message.req == MaxIndependentSet.Request.ACTIVATE) {
			 treatmentDone = handleActivateRequest(message);
			
		} else if (message.req == MaxIndependentSet.Request.DEACTIVATE){
			treatmentDone = handleDeactivateRequest(message);
		}
		
		if ( !treatmentDone && !inbox.hasNext()) {
			addNodeToMaxUGroup(message);
		}
	}
	
	private void addNodeToMaxUGroup(MaxIndependentSet message) {
		// active and handle all neighbor messages => in U
		U.add(this);
		setColor(Color.BLUE);
		deactivtorNode = this;
		for(Edge e : outgoingConnections) {
			send(new MaxIndependentSet(MaxIndependentSet.Request.DEACTIVATE, maxu_num, this), e.endNode);
		}
		
	}

	private boolean handleDeactivateRequest(MaxIndependentSet message) {
		if ( (message.num > maxu_num)  || (message.num == maxu_num) && message.node.ID > this.ID) {
			setColor(Color.RED);
			deactivtorNode =  message.node;
			MyNode.U.remove(this);
			finished = true;
			return true;
		}
		return false;
	}

	private boolean handleActivateRequest(MaxIndependentSet message) {
		setColor(Color.GREEN);
		if(message.num > maxu_num) {
			inbox.reset();
			for(Edge e : outgoingConnections) {
				send(new MaxIndependentSet(MaxIndependentSet.Request.ACTIVATE, maxu_num, this), e.endNode);
			}
			return true;
		}
		return false;
	}

	private void handleInitRequest(MaxIndependentSet message) {
		maxu_num = message.num;
		finished = false;
		MyNode.U.clear();
		for(Edge e : outgoingConnections) {
			send(new MaxIndependentSet(MaxIndependentSet.Request.ACTIVATE, maxu_num, this), e.endNode);
		}
		
	}

	private long maxu_num;
	private boolean finished;
	public Node deactivtorNode;
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message message = inbox.next();
			if(message instanceof ShortestPathInU) {
				shortestPathInUHandler((ShortestPathInU)message);
			}
			if(message instanceof DeliverMsg) {
				deliverMsgHandler((DeliverMsg)message);
			}
			if(message instanceof MaxIndependentSet) {
				maxIndependentSetHandler((MaxIndependentSet)message);
				return;
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
					return; // aborted
				}
				MyNode destiation = (MyNode)n;
				MyTimer t = new MyTimer(new DeliverMsg(destiation, destiation.deactivtorNode));
				t.startRelative(1, MyNode.this);
			}
		}, "Select a node to send a message to...");
	}
	
	
	public void sendDirectWrapper(Message msg) {
		sendDirect(msg, this);
	}
	
	public void resetParentTable() {
		parentTable = new Hashtable<Node, ParentDistnace>();
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
