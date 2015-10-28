package jmetal.util.offspring;

import java.util.HashMap;

import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class BitFlipMutationOffspring extends Offspring {
    private Operator mutation_;
    private Operator selection_;

    private double mutationProbability_;
    private double distributionIndex_;

    public BitFlipMutationOffspring(double mutationProbability,
	    double distributionIndexForMutation) throws JMException {
	HashMap parameters; // Operator parameters
	parameters = new HashMap();
	parameters.put("probability",
		mutationProbability_ = mutationProbability);
	parameters.put("distributionIndex",
		distributionIndex_ = distributionIndexForMutation);
	mutation_ = MutationFactory.getMutationOperator("BitFlipMutation",
		parameters);

	selection_ = SelectionFactory.getSelectionOperator("BinaryTournament2",
		null);
	id_ = "BitFlipMutation";
    }

    public Solution getOffspring(Solution solution) {
	Solution res = new Solution(solution);
	try {
	    mutation_.execute(res);
	} catch (JMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return res;
    }

    public String configuration() {
	String result = "-----\n";
	result += "Operator: " + id_ + "\n";
	result += "Probability: " + mutationProbability_ + "\n";
	result += "DistributionIndex: " + distributionIndex_;

	return result;
    }
}
