/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.result;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container class for the results of a competition.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 * 
 */
public class CompetitionResult {
	Map<Integer, List<PASTACompUserResult>> positions;

	public Map<Integer, List<PASTACompUserResult>> getPositions() {
		return positions;
	}

	public void setPositions(Map<Integer, List<PASTACompUserResult>> positions) {
		this.positions = positions;
	}
	
	public void updatePositions(List<PASTACompUserResult> users){
		if(!users.isEmpty()){
			// clear
			if(positions == null){
				positions = new TreeMap<Integer, List<PASTACompUserResult>>();
			}
			positions.clear();
			
			// sort
			Collections.sort(users);
			
			// array up
			Double bestSoFar = users.get(0).getPercentage();
			int currPos = 1;
			for(int i=0; i<users.size(); ++i){
				if(bestSoFar.compareTo(users.get(i).getPercentage()) != 0){
					++currPos;
					bestSoFar = users.get(i).getPercentage();
				}
				
				if(!positions.containsKey(currPos)){
					positions.put(currPos, new LinkedList<PASTACompUserResult>());
				}
				
				positions.get(currPos).add(users.get(i));
			}
		}
	}
}
