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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;

import javax.swing.JOptionPane;

import projects.defaultProject.nodes.timers.DirectMessageTimer;
import projects.defaultProject.nodes.timers.MessageTimer;
import projects.sample5.nodes.messages.MaxU;
import projects.sample5.nodes.messages.PathU;
import projects.sample5.nodes.nodeImplementations.FNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.runtime.SynchronousRuntimeThread;
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
	
	static public int numOfRoundsForStepOne = 0;
	static public int numOfRoundsForStepTwo = 0;
	static public int numOfRoundTotal = 0;
	public boolean hasTerminated() {
		return false;
	}

	@AbstractCustomGlobal.CustomButton(buttonText="Generate routing table", toolTipText="Generate routing table")
	public void sampleButton() {
		Global.systemState = 1;
		Random rand = new Random();
		double max = Tools.getNodeList().size();
		for(Node n : Tools.getNodeList()) {
			double rand_maxu = rand.nextDouble(Math.pow(max,4) + 1);
			DirectMessageTimer t = new DirectMessageTimer(new MaxU(MaxU.Request.INIT, rand_maxu, n), n);
			t.startRelative(1, n);
		}
	}
	
	static public void genPath() throws IOException {
		for(Node n : Tools.getNodeList()) {
			if(n instanceof FNode) {
				((FNode)n).resetParentTable();
			}
		}
		
		for(FNode n : FNode.U) {	
			DirectMessageTimer t = new DirectMessageTimer(new PathU(n,n,0.0,0), n);
			t.startRelative(1, n);
		}
		Global.systemState = 2;
	}
	
	static public void toCsv() throws IOException
	{
		int maxDegree = 0;
		for(Node n : Tools.getNodeList()) {
			if(n.outgoingConnections.size() > maxDegree)
			{
				maxDegree = n.outgoingConnections.size(); // Find max degree
			}
		}
		
		String strNumOfNodes = "Number vertices in a graph: " + Tools.getNodeList().size() + "\n";
		String strSizeOfU = "Size of U: " + FNode.U.size() + "\n";
		String strGenerateU = "Number of rounds for building U: " + numOfRoundsForStepOne + "\n";
		String strGeneratePath = "Number of rounds for building Path: " + numOfRoundsForStepTwo + "\n";
		String strSendMessage = "Number of rounds for sending message: " + numOfRoundTotal + "\n";
		String strMaxDegree = "Max degree in a graph is : " + maxDegree + "\n";
		int radious = Collections.max(FNode.numOfSendedMessagesFromNode.values());
		String strDiameter = "Diameter of a graph: " + radious * 2 + "\n";
		
		String header = "num_v, max_deg_v, num_v_in_u, leng_path, approximation_diameter, rounds_build_u_and_build_path\n";
		String line = 
				Tools.getNodeList().size() + ", " +
				maxDegree + ", " +
				FNode.U.size() + ", " +
				String.valueOf(numOfRoundTotal - numOfRoundsForStepTwo) + ", " +
				radious  + ", " +
				numOfRoundsForStepTwo;
				
		BufferedWriter writer = new BufferedWriter(new FileWriter("result.csv"));
		writer.write(header);
		writer.append(line);
		writer.close();
	
	}
	
}

