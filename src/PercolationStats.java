/**
 * The <tt>PercolationStats<tt> class supports estimation of the percolation 
 * threshold for a <em>N-by-N<em> grid via statistical methods.
 * <p>
 * It performs <em>T<em> independent percolation experiments on a 
 * <em>N-by-N<em> grid, and prints out the <em>mean<em>, 
 * <em>standard deviation<em>, and <em>the 95% confidence interval<em>
 * for the percolation threshold.
 * <p>
 * To execute this class, simply run:
 * <code>
 * <java PercolationStats N T
 * <code> 
 * where,
 * <em>N<em> represents the grid size, and
 * <em>T<em> is the number of times the percolation experiment should run
 * 
 * @author Igor Arouca
 * @version 1.0
 * @since 2015-02-10
 *
 */
public class PercolationStats {

	// 95% confidence interval z-value set as default coefficient
	private static final double DEFAULT_Z_VALUE = 1.96;

	private int gridSize;				// if gridSize is N, grid is N-by-N
	private int totalSites;				// total number of sites
	private int numberOfExperiments;	// number of experiment executions

	private double[] estimates;			// percolation threshold estimates

	/**
	 *  Performs T independent experiments on a N-by-N grid
	 *  
	 * @param N size of the grid used to simulate the percolation experiments
	 * @param T number of times the experiment will be executed
	 */
	public PercolationStats(int N, int T) {
		if (N <= 0) {
			throw new IllegalArgumentException(
				"Invalid grid size: " + N + ". N has to be greater than zero."
			);
		}

		if (T <= 0) {
			throw new IllegalArgumentException(
				"Invalid number of experiments: " + T + 
				". T has to be greater than zero."
			);
		}

		this.gridSize = N;
		this.totalSites = N * N;
		this.numberOfExperiments = T;

		this.estimates = getPercolationThresholdEstimates();
	}

	private double[] getPercolationThresholdEstimates() {
		double[] estimates = new double[numberOfExperiments];

		for (int i = 0; i < numberOfExperiments; ++i) {
			estimates[i] = getPercolationThresholdEstimate();
		}

		return estimates;
	}

	private double getPercolationThresholdEstimate() {
		Percolation percolation = new Percolation(gridSize);
		int openSitesCount = 0;
		int i, j;

		while(!percolation.percolates()) {
			do {
				i = StdRandom.uniform(1, gridSize + 1);
				j = StdRandom.uniform(1, gridSize + 1);

			} while(percolation.isOpen(i, j));

			percolation.open(i, j);
			++openSitesCount;
		}

		return (double) openSitesCount / totalSites;
	}

	/**
	 *  Sample mean of percolation threshold
	 * @return
	 */
	public double mean() {
		return StdStats.mean(estimates);
	}

	/**
	 *  Sample standard deviation of percolation threshold
	 * @return
	 */
	public double stddev() {
		return StdStats.stddev(estimates);
	}

	/**
	 *  Low end-point of 95% confidence interval
	 * @return
	 */
	public double confidenceLo() {
		return mean() - halfConfidenceInterval();
	}

	/**
	 * High end-point of 95% confidence interval
	 * @return
	 */
	public double confidenceHi() {
		return mean() + halfConfidenceInterval();
	}

	private double halfConfidenceInterval() {
		return DEFAULT_Z_VALUE * stddev() / Math.sqrt(numberOfExperiments);
	}

	/**
	 * Requests user input for grid size (N) and number of iterations (T) then
	 * executes percolation simulation and print results: mean, std deviation,
	 * and 95% confidence interval.
	 * 
	 * @param args contains 2 arguments: 1) N (grid size) and 2) T (iterations)
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Usage: java PercolateStats N T");
			System.out.println("N: grid size");
			System.out.println("T: number of percolation experiments");

			return;
		}

		int N = Integer.parseInt(args[0]);
		int T = Integer.parseInt(args[1]);

		PercolationStats percolationStats = new PercolationStats(N, T);

		System.out.printf("%-23s = %.16f\n", "mean", percolationStats.mean());

		System.out.printf(
			"%-23s = %.16f\n", "stddev", percolationStats.stddev()
		);

		System.out.printf(
			"%-23s = %.16f, %.16f\n\n", "95% confidence interval",
			percolationStats.confidenceLo(), percolationStats.confidenceHi()
		);
	}

}