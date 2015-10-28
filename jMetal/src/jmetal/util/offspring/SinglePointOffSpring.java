package jmetal.util.offspring;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.SinglePointCrossover;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class SinglePointOffSpring extends Offspring {
    private Double crossoverProbability_ = null;
    private Double distributionIndex = null;

    private Operator crossover_;
    private Operator selection_;

    public SinglePointOffSpring(double probability, double distributionIndex) {
	HashMap parameters = null;
	try {
	    // crossover operator
	    parameters = new HashMap();
	    parameters.put("probability", probability);
	    parameters.put("distributionIndex", distributionIndex);
	    crossover_ = new SinglePointCrossover(parameters);

	    // Selection operator
	    parameters = null;
	    selection_ = SelectionFactory.getSelectionOperator(
		    "BinaryTournament2", parameters);
	} catch (JMException ex) {
	    Logger.getLogger(SinglePointOffSpring.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
	id_ = "SingePoint";
    }

    public Solution getOffspring(SolutionSet solutionSet, int index) {
	Solution[] parents = new Solution[3];
	Solution offSpring = null;

	try {
	    int r1, r2;
	    do {
		r1 = PseudoRandom.randInt(0, solutionSet.size() - 1);
	    } while (r1 == index);
	    do {
		r2 = PseudoRandom.randInt(0, solutionSet.size() - 1);
	    } while (r2 == index || r2 == r1);

	    parents[0] = solutionSet.get(r1);
	    parents[1] = solutionSet.get(r2);
	    parents[2] = solutionSet.get(index);

	    Solution[] children = (Solution[]) crossover_.execute(parents);
	    offSpring = children[0];
	} catch (JMException ex) {
	    Logger.getLogger(SinglePointOffSpring.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
	return offSpring;
    }

    /**
     * 
     * 
     */
    public Solution getOffspring(Solution[] parentSolutions,
	    Solution currentSolution) {
	Solution[] parents = new Solution[3];
	Solution offspring = null;
	try {
	    parents[0] = parentSolutions[0];
	    parents[1] = parentSolutions[1];
	    parents[2] = currentSolution;

	    offspring = (Solution) crossover_.execute(new Object[] {
		    currentSolution, parents });
	} catch (JMException ex) {
	    Logger.getLogger(SinglePointOffSpring.class.getName()).log(
		    Level.SEVERE, null, ex);
	}
	return offspring;
    }
    
    public String configuration() {
	    String result = "-----\n" ;
	    result += "Operator: " + id_ + "\n" ;
	    result += "probability: " + crossoverProbability_ + "\n" ;
	    result += "distribution index: " + distributionIndex ;

	    return result ;
	  }
}
