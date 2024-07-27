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

import projects.sample5.nodes.messages.MaxU;
import projects.sample5.nodes.messages.PathU;
import projects.sample5.nodes.nodeImplementations.MyNode;
import projects.sample5.nodes.timers.GTimer;
import projects.sample6.nodes.messages.MarkMessage;
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
	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public static int maxDegree = 0;
	static public int numOfRoundsForStepOne = 0;
	static public int numOfRoundsForStepTwo = 0;
	static public int numOfRoundsForStepThree = 0;
	public boolean hasTerminated() {
		return false;
	}
	
	@GlobalMethod(menuText="Reset Node Color")
	public void resetNodeColor() {
		for(Node n : Tools.getNodeList()) {
			n.setColor(Color.BLACK);
		}
	}
	
	@AbstractCustomGlobal.CustomButton(buttonText="GEN-U & PATHs", toolTipText="A sample button")
	public void sampleButton() {
		Global.systemState = 1;
		Random rand = new Random();
		int max = Tools.getNodeList().size();
		for(Node n : Tools.getNodeList()) {
			if(n.outgoingConnections.size() > maxDegree)
			{
				maxDegree = n.outgoingConnections.size(); // Find max degree
			}
			double rand_maxu = rand.nextInt((int)Math.pow(max,4) + 1);
			GTimer t = new GTimer(new MaxU(MaxU.Request.INIT, rand_maxu, n));
			t.startRelative(1, n);
		}
	}
	
	//@AbstractCustomGlobal.CustomButton(buttonText="GEN-Path", toolTipText="A sample button")
	static public void genPath() throws IOException {
		for(Node n : Tools.getNodeList()) {
			if(n instanceof MyNode) {
				((MyNode)n).resetParentTable();
			}
		}
		
		for(MyNode n : MyNode.U) {	
			GTimer t = new GTimer(new PathU(n,n,0.0,0));
			t.startRelative(1, n);
		}
		Global.systemState = 2;
	}
	
	static public void writeStatisticsToFile() throws IOException
	{
		String strNumOfNodes = "Number vertices in a graph: " + Tools.getNodeList().size() + "\n";
		String strSizeOfU = "Size of U: " + MyNode.U.size() + "\n";
		String strGenerateU = "Number of rounds for building U: " + numOfRoundsForStepOne + "\n";
		String strGeneratePath = "Number of rounds for building Path: " + numOfRoundsForStepTwo + "\n";
		String strSendMessage = "Number of rounds for sending message: " + numOfRoundsForStepThree + "\n";
		String strMaxDegree = "Max degree in a graph is : " + maxDegree + "\n";
		int radious = Collections.max(MyNode.numOfSendedMessagesFromNode.values());
		String strDiameter = "Diameter of a graph: " + radious * 2 + "\n";
		String totalNumOfRounds = "The total number of rounds is : " + (numOfRoundsForStepOne + numOfRoundsForStepTwo + numOfRoundsForStepThree);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("Statistics.txt"));
		writer.write(strNumOfNodes);
		writer.append(strMaxDegree);
		writer.append(strSizeOfU);
		writer.append(strSendMessage);
		writer.append(strDiameter);
		writer.append(totalNumOfRounds);
		
		writer.close();
	
	}
	
}

