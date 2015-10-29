/**
 * This class is based on JMetal pNSGAII.java, any copyrights can refer to pNSGAII.java
 * This class implements a mutation operator whose mutation probability is adaptively update
 * based on the crowding distances and generation number
 * 
 */
package jmetal.metaheuristics.nsgaII;

import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;

/**
 * Implementation of NSGA-II. This implementation of NSGA-II makes use of a
 * QualityIndicator object to obtained the convergence speed of the algorithm.
 * This version is used in the paper: A.J. Nebro, J.J. Durillo, C.A. Coello
 * Coello, F. Luna, E. Alba
 * "A Study of Convergence Speed in Multi-Objective Metaheuristics." To be
 * presented in: PPSN'08. Dortmund. September 2008.
 */

public class NSGAIIMutationAdaptive extends Algorithm {
    /*
     * The two parameters that determines the probability of bitflipmutation
     * operator
     */
    private double delta_dist;
    private double generation;

    /**
     * Constructor
     * 
     * @param problem
     *            Problem to solve
     */
    public NSGAIIMutationAdaptive(Problem problem) {
	super(problem);
    } // NSGAII

    /**
     * Runs the NSGA-II algorithm.
     * 
     * @return a <code>SolutionSet</code> that is a set of non dominated
     *         solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
	int populationSize;
	int maxEvaluations;
	int evaluations;

	QualityIndicator indicators; // QualityIndicator object
	int requiredEvaluations; // Use in the example of use of the
	// indicators object (see below)

	SolutionSet population;
	SolutionSet offspringPopulation;
	SolutionSet union;

	Operator mutationOperator;
	Operator crossoverOperator;
	Operator selectionOperator;

	Distance distance = new Distance();

	// Read the parameters
	populationSize = ((Integer) getInputParameter("populationSize"))
		.intValue();
	maxEvaluations = ((Integer) getInputParameter("maxEvaluations"))
		.intValue();
	indicators = (QualityIndicator) getInputParameter("indicators");

	// Initialize the variables
	population = new SolutionSet(populationSize);
	evaluations = 0;

	requiredEvaluations = 0;

	// Read the operators
	mutationOperator = operators_.get("mutation");
	crossoverOperator = operators_.get("crossover");
	selectionOperator = operators_.get("selection");

	// Create the initial solutionSet
	Solution newSolution;
	for (int i = 0; i < populationSize; i++) {
	    newSolution = new Solution(problem_);
	    problem_.evaluate(newSolution);
	    problem_.evaluateConstraints(newSolution);
	    evaluations++;
	    population.add(newSolution);
	} // for

	// Generations
	while (evaluations < maxEvaluations) {
	    
	    // TODO update the mutation probability
	    

	    // Create the offSpring solutionSet
	    offspringPopulation = new SolutionSet(populationSize);
	    Solution[] parents = new Solution[2];
	    for (int i = 0; i < (populationSize / 2); i++) {
		if (evaluations < maxEvaluations) {
		    // obtain parents
		    parents[0] = (Solution) selectionOperator
			    .execute(population);
		    parents[1] = (Solution) selectionOperator
			    .execute(population);
		    Solution[] offSpring = (Solution[]) crossoverOperator
			    .execute(parents);
		    mutationOperator.execute(offSpring[0]);
		    mutationOperator.execute(offSpring[1]);
		    problem_.evaluate(offSpring[0]);
		    problem_.evaluateConstraints(offSpring[0]);
		    problem_.evaluate(offSpring[1]);
		    problem_.evaluateConstraints(offSpring[1]);
		    offspringPopulation.add(offSpring[0]);
		    offspringPopulation.add(offSpring[1]);
		    evaluations += 2;
		} // if
	    } // for

	    // Create the solutionSet union of solutionSet and offSpring
	    union = ((SolutionSet) population).union(offspringPopulation);

	    // Ranking the union
	    Ranking ranking = new Ranking(union);

	    int remain = populationSize;
	    int index = 0;
	    SolutionSet front = null;
	    population.clear();

	    // Obtain the next front
	    front = ranking.getSubfront(index);

	    while ((remain > 0) && (remain >= front.size())) {
		// Assign crowding distance to individuals
		distance.crowdingDistanceAssignment(front,
			problem_.getNumberOfObjectives());
		// Add the individuals of this front
		for (int k = 0; k < front.size(); k++) {
		    population.add(front.get(k));
		} // for

		// Decrement remain
		remain = remain - front.size();

		// Obtain the next front
		index++;
		if (remain > 0) {
		    front = ranking.getSubfront(index);
		} // if
	    } // while

	    // Remain is less than front(index).size, insert only the best one
	    if (remain > 0) { // front contains individuals to insert
		distance.crowdingDistanceAssignment(front,
			problem_.getNumberOfObjectives());
		front.sort(new CrowdingComparator());
		for (int k = 0; k < remain; k++) {
		    population.add(front.get(k));
		} // for

		remain = 0;
	    } // if
	    
	    //TODO update generation and distance between best and worst solution
	    generation ++; //the generation is increased by 1
	    
	    double max = Double.MIN_VALUE;
	    double min = Double.MAX_VALUE;
	    for(int k=0; k<populationSize; k++){
		double dist = population.get(k).getCrowdingDistance();
		if(dist>max){
		    max = dist;
		}
		if(dist<min){
		    min = dist;
		}
	    }
	    
	    //mutation updates
	    delta_dist = (max-min)/max;//normalize distance
	    //the probability is calculate by (1- (1/(1+e^-0.07t))) * delta_dist
	    double probability = (1-(1/(1+Math.pow(Math.exp(1.0), -0.07*generation))) * delta_dist);
	    mutationOperator.setParameter("probability", probability);//update probability
	    
	    
	    // This piece of code shows how to use the indicator object into the
	    // code
	    // of NSGA-II. In particular, it finds the number of evaluations
	    // required
	    // by the algorithm to obtain a Pareto front with a hypervolume
	    // higher
	    // than the hypervolume of the true Pareto front.
	    if ((indicators != null) && (requiredEvaluations == 0)) {
		double HV = indicators.getHypervolume(population);
		if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
		    requiredEvaluations = evaluations;
		} // if
	    } // if
	} // while

	// Return as output parameter the required evaluations
	setOutputParameter("evaluations", requiredEvaluations);

	// Return the first non-dominated front
	Ranking ranking = new Ranking(population);
	ranking.getSubfront(0).printFeasibleFUN("FUN_NSGAII");
	ranking.getSubfront(0).printFeasibleVAR("VAR_NSGAII");

	return ranking.getSubfront(0);
    } // execute
} // NSGA-II
