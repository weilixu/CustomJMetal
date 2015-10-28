package jmetal.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class implements a bit flip mutation operator. It is adaptive and
 * accepts binary or integer solutions
 * 
 * @author Weili
 *
 */
public class BitFlipMutationAdaptive extends Mutation {
    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(
	    BinarySolutionType.class, BinaryRealSolutionType.class,
	    IntSolutionType.class);
    private Double mutationProbability_ = null;

    public BitFlipMutationAdaptive(HashMap<String, Object> parameters) {
	super(parameters);
	if (parameters.get("probability") != null)
	    mutationProbability_ = (Double) parameters.get("probability");
    }

    /**
     * Perform the mutation operation
     * 
     * @param probability
     *            Mutation probability
     * @param solution
     *            The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution)
	    throws JMException {
	try {
	    if ((solution.getType().getClass() == BinarySolutionType.class)
		    || (solution.getType().getClass() == BinaryRealSolutionType.class)) {
		for (int i = 0; i < solution.getDecisionVariables().length; i++) {
		    for (int j = 0; j < ((Binary) solution
			    .getDecisionVariables()[i]).getNumberOfBits(); j++) {
			if (PseudoRandom.randDouble() < probability) {
			    ((Binary) solution.getDecisionVariables()[i]).bits_
				    .flip(j);
			}
		    }
		}

		for (int i = 0; i < solution.getDecisionVariables().length; i++) {
		    ((Binary) solution.getDecisionVariables()[i]).decode();
		}
	    } // if
	    else { // Integer representation
		for (int i = 0; i < solution.getDecisionVariables().length; i++)
		    if (PseudoRandom.randDouble() < probability) {
			int value = PseudoRandom.randInt((int) solution
				.getDecisionVariables()[i].getLowerBound(),
				(int) solution.getDecisionVariables()[i]
					.getUpperBound());
			solution.getDecisionVariables()[i].setValue(value);
		    } // if
	    } // else
	} catch (ClassCastException e1) {
	    Configuration.logger_.severe("BitFlipMutation.doMutation: "
		    + "ClassCastException error" + e1.getMessage());
	    Class cls = java.lang.String.class;
	    String name = cls.getName();
	    throw new JMException("Exception in " + name + ".doMutation()");
	}
    } // doMutation

    /**
     * Executes the operation
     * 
     * @param object
     *            An object containing a solution to mutate
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
	
	Solution solution = (Solution) object;

	if (!VALID_TYPES.contains(solution.getType().getClass())) {
	    Configuration.logger_
		    .severe("BitFlipMutation.execute: the solution "
			    + "is not of the right type. The type should be 'Binary', "
			    + "'BinaryReal' or 'Int', but "
			    + solution.getType() + " is obtained");

	    Class cls = java.lang.String.class;
	    String name = cls.getName();
	    throw new JMException("Exception in " + name + ".execute()");
	} // if
	
	/*
	 * re-read the updated probability from parameter
	 */
	mutationProbability_ = (Double) parameters_.get("probability");
	doMutation(mutationProbability_, solution);
	return solution;
    } // execute

}
