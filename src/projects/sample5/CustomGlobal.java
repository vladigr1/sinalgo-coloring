/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.sample5;


import java.awt.Color;
import java.util.Random;

import javax.swing.JOptionPane;

import projects.sample5.nodes.messages.MaxU;
import projects.sample5.nodes.messages.PathU;
import projects.sample5.nodes.nodeImplementations.FNode;
import projects.sample5.nodes.timers.GTimer;
import projects.sample6.nodes.messages.MarkMessage;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

//	@GlobalMethod(menuText="Clear Routing Tables")
//	public void clearRoutingTalbes() {
//		for(Node n : Tools.getNodeList()) {
//			FNode fn = (FNode) n;
//			fn.clearRoutingTable();
//		}
//	}
	
	@GlobalMethod(menuText="Reset Node Color")
	public void resetNodeColor() {
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.BLACK);
		}
	}
	
	@AbstractCustomGlobal.CustomButton(buttonText="GEN-U", toolTipText="A sample button")
	public void sampleButton() {
		Random rand = new Random();
		int max = Tools.getNodeList().size();
		for(Node n : Tools.getNodeList()) {
			int rand_maxu = rand.nextInt((int)Math.pow(max,4) + 1);
			GTimer t = new GTimer(new MaxU(MaxU.Request.INIT, rand_maxu, n.ID));
			t.startRelative(1, n);
		}
	}
	
	@AbstractCustomGlobal.CustomButton(buttonText="GEN-Path", toolTipText="A sample button")
	public void genPath() {
		for(Node n : Tools.getNodeList()) {
			if(n instanceof FNode) {
				((FNode)n).resetParentTable();
			}
		}
		
		for(FNode n : FNode.U) {
			GTimer t = new GTimer(new PathU(n,n,0.0));
			t.startRelative(1, n);
		}
	}
	
}
