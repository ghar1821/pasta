package pasta.domain.result;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pasta.domain.PASTACompUserResult;

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
