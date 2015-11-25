package jmetal.metaheuristics.nsgaII;

import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.parallel.IParallelEvaluator;

/**
 * Not completed yet
 * 
 * @author Weili
 *
 */
public class pNSGAIIAdaptive extends Algorithm {

    IParallelEvaluator parallelEvaluator_;
    private int generation = 0;// start from first generation
    private int realSimuN;
    private int circleDivider;

    /**
     * Constructor
     * 
     * @param problem
     *            Problem to solve
     * @param evaluator
     *            Parallel evaluator
     */
    public pNSGAIIAdaptive(Problem problem, IParallelEvaluator evaluator,
	    int realSimu, int divider) {
	super(problem);

	parallelEvaluator_ = evaluator;
	realSimuN = realSimu;
	circleDivider = divider;
    } // pNSGAIIAdaptive

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
	int numberOfThreads;

	QualityIndicator indicators; // QualityIndicator object
	int requiredEvaluations; // Use in the example of use of the
	// indicators object (see below)

	SolutionSet population;
	SolutionSet offspringPopulation;
	SolutionSet union;
	SolutionSet simuPopulation;
	SolutionSet regressPopulation;

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

	parallelEvaluator_.startEvaluator(problem_);

	// Initialize the variables
	population = new SolutionSet(populationSize);
	simuPopulation = new SolutionSet(populationSize);
	regressPopulation = new SolutionSet(populationSize);
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
	    parallelEvaluator_.addSolutionForEvaluation(newSolution);
	}

	List<Solution> solutionList = parallelEvaluator_.parallelEvaluation();
	for (Solution solution : solutionList) {
	    population.add(solution);
	    simuPopulation.add(solution);// fill in simulation population
	    evaluations++;
	}

	// Generations
	while (evaluations < maxEvaluations) {
	    // identify the generation for regression or real simulation first
	    generation++; // the generation is increased by 1
	    int newGenCounter = generation % circleDivider;

	    // Create the offSpring solutionSet
	    offspringPopulation = new SolutionSet(populationSize);
	    Solution[] parents = new Solution[2];
	    if (newGenCounter == 0) {
		for(int i=0; i<populationSize; i++){
		    parallelEvaluator_.addSolutionForEvaluation(population.get(i));
		}
	    } else {
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
			parallelEvaluator_
				.addSolutionForEvaluation(offSpring[0]);
			parallelEvaluator_
				.addSolutionForEvaluation(offSpring[1]);
		    } // if
		} // for
	    }// if
	    List<Solution> solutions = parallelEvaluator_.parallelEvaluation();

	    for (Solution solution : solutions) {
		offspringPopulation.add(solution);
		evaluations++;
	    }

	    // Create the solutionSet union of solutionSet and offSpring
	    if (newGenCounter < realSimuN) {
		// case when regression switch to real simulation
		union = ((SolutionSet) simuPopulation)
			.union(offspringPopulation);
	    } else {
		if (regressPopulation.size() == 0) {
		    // case when the first time using regression, copy
		    // population
		    for (int j = 0; j < population.size(); j++) {
			regressPopulation.add(population.get(j));
		    }
		}
		union = ((SolutionSet) regressPopulation)
			.union(offspringPopulation);
	    }

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

	    if (newGenCounter < realSimuN) {
		// case when real simulation is required
		simuPopulation.clear();
		for (int j = 0; j < population.size(); j++) {
		    simuPopulation.add(population.get(j));
		}

	    } else {
		// case when regression simulation is required
		regressPopulation.clear();
		for (int j = 0; j < population.size(); j++) {
		    regressPopulation.add(population.get(j));
		}
	    }
	    population
		    .printFeasibleFUN("E:\\02_Weili\\02_ResearchTopic\\Optimization\\ParetoFronts\\Front"
			    + generation);

	    // System.out.println("Here is the distance between best and worst: "
	    // + delta_dist);

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

	parallelEvaluator_.stopEvaluator();

	// Return as output parameter the required evaluations
	setOutputParameter("evaluations", requiredEvaluations);

	// Return the first non-dominated front
	Ranking ranking = new Ranking(population);
	return ranking.getSubfront(0);
    } // execute
}
