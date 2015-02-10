
/**
 * 
 * @author Igor Arouca
 */
public class Percolation {

	private Grid grid;

	// create N-by-N grid, with all sites blocked
	/**
     * Initializes an empty Percolation data structure with (N * N) sites.
     * All sites are initially blocked (status = 0).
     * @throws java.lang.IllegalArgumentException if N <= 0
     * @param N the number of objects
     */
	public Percolation(int N) {
		this.grid = new Grid(N);
	}

	/** Open site (i, j) if it is not open already
	 * 
	 * @param i
	 * @param j
	 */
	public void open(int i, int j) {
		grid.getSiteAt(i, j).open();
	}

	/** Is site (i, j) open?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is open
	 */
	public boolean isOpen(int i, int j) {
		return grid.getSiteAt(i, j).isOpen();
	}

	/** Is site (i, j) full?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is full
	 */
	public boolean isFull(int i, int j) {
		return grid.getSiteAt(i, j).isFull();
	}

	/** Does the system percolate?
	 * The system percolates if there is a full site in the bottom row.
	 * 
	 * @return true if the system percolates
	 */
	public boolean percolates() {
		return grid.percolates();
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

	}

}

/**
 * The <tt>Grid</tt> class represents a 2-dimensional grid of sites. 
 * Each site is represented by an instance of <code>Site</code>.
 * This implementation supports union-find operations on the grid's sites
 * 
 * @author Igor Arouca
 */
class Grid {

	private static final int VIRTUAL_TOP_SITE_INDEX = 0;

	private int size;				// number of sites
	private int origin;				// X and Y axes origin

	private Site[][] sites;			// 2-dimensional grid of sites

	// Data structure to support union-find operations on the grid.
	// If there was an interface UnionFind, this object could be dynamically set
	private WeightedQuickUnionUF unionFind;

	/**
	 * Initializes a n-by-n grid with all sites initially blocked.
	 * The origin is at the top-left corner (1,1),
	 * with X-axis pointing right and Y-axis pointing down
	 *  
	 * @param n the size of this 2-dimensional grid (n-by-n) 
	 */
	Grid(int n) {
		this(1, n);
	}

	/**
	 * Initializes a n-by-n grid with all sites initially blocked.
	 * The origin defines the initial value for both axes.
	 * For instance, a value of zero for the origin (origin = 0), 
	 * would set the grid origin to (0,0) 
	 * 
	 * @param origin the origin of both x and y axes. 
	 * @param n the size of this 2-dimensional grid (n-by-n)
	 */
	Grid(int origin, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException(
				"Invalid grid size: " + n + 
				". Grid size (n) has to be greater than zero."
			);
		}

		this.origin = origin;
		this.size = n;

		initializeSites();
		initializeUnionFindStructure(n);
	}

	private void initializeSites() {
		int length = origin + size;

		for (int i = origin; i < length; ++i) {
			for (int j = origin; j < length; ++j) {
				sites[i][j] = new Site(this, i, j);
			}
		}
	}

	private void initializeUnionFindStructure(int n) {

		// creates union-find structure with 2 extra nodes for virtual sites
		this.unionFind = new WeightedQuickUnionUF(n + 2);

		createConnectionsToVirtualTopSite();
		createConnectionsToVirtualBottomSite();
	}

	private void createConnectionsToVirtualTopSite() {
		Site[] topRowSites = sites[size];

		for (Site site : topRowSites) {
			unionFind.union(
				getVirtualTopSiteIndex() , site.getUnionFindIndex()
			);
		}
	}

	int getVirtualTopSiteIndex() {
		return VIRTUAL_TOP_SITE_INDEX;
	}

	private void createConnectionsToVirtualBottomSite() {
		Site[] bottomRowSites = sites[origin];

		for (Site site : bottomRowSites) {
			unionFind.union(
				getVirtualBottomSiteIndex(), site.getUnionFindIndex()
			);
		}
	}

	private int getVirtualBottomSiteIndex() {
		return size + 1;
	}

	/**
	 * Return integer representing origin of both axes.
	 * If returned value is 1, it means the grid's origin is (1,1)
	 * 
	 * @return origin of the grid
	 */
	public int getOrigin() {
		return origin;
	}

	/**
	 * Get site at provided location (x,y)
	 * 
	 * @param x coordinate in the x-axis
	 * @param y coordinate in the y-axis
	 * @return site at location (x,y)
	 */
	
	public Site getSiteAt(int x, int y) {
		return sites[x][y];
	}

	/**
	 * Get size of the grid.
	 * If size = n, grid is n-by-n
	 * 
	 * @return size of the grid
	 */
	public int getSize() {
		return sites.length;
	}

	WeightedQuickUnionUF getUnionFind() {
		return unionFind;
	}

	public boolean percolates() {
		return unionFind.connected(
			getVirtualBottomSiteIndex(), getVirtualTopSiteIndex()
		);
	}

}

class Site {

	private static enum Status { BLOCKED, OPEN };

	private int x;
	private int y;

	private Grid grid;

	private Status status;

	Site(Grid grid, int x, int y) {
		this.x = x;
		this.y = y;

		this.grid = grid;

		this.status = Status.BLOCKED;
	}

	public boolean isFull() {
		return grid.getUnionFind().connected(
			grid.getVirtualTopSiteIndex(), this.getUnionFindIndex()
		);
	}

	public boolean isOpen() {
		return status == Status.OPEN;
	}

	public void open() {
		if (isOpen()) return;

		this.status = Status.OPEN;
		connectToOpenAdjacentSites();
	}

	public void connectToOpenAdjacentSites() {
		connectTo(getTopAdjacentSite());
		connectTo(getRightAdjacentSite());
		connectTo(getBottomAdjacentSite());
		connectTo(getLeftAdjacentSite());
	}

	public void connectTo(Site adjacent) {
		if (adjacent == null || !adjacent.isOpen()) return;

		grid.getUnionFind().union(
			getUnionFindIndex(), adjacent.getUnionFindIndex());
	}

	/**
	 * 
	 * @return index of the Site in the union-find structure 
	 */
	public int getUnionFindIndex() {		
		return ((grid.getSize() * x) + y) + 1;
	}

	public Site getLeftAdjacentSite() {
		Site adjacent = null;

		if (x > grid.getOrigin()) {
			adjacent = grid.getSiteAt(x - 1, y);
		}

		return adjacent;
	}

	public Site getRightAdjacentSite() {
		Site adjacent = null;

		if (x < grid.getSize()) {
			grid.getSiteAt(x + 1, y);
		}

		return adjacent;
	}

	public Site getTopAdjacentSite() {
		Site adjacent = null;

		if (y > grid.getOrigin()) {
			adjacent = grid.getSiteAt(x, y - 1);
		}

		return adjacent;
	}

	public Site getBottomAdjacentSite() {
		Site adjacent = null;

		if (y < grid.getSize()) {
			adjacent = grid.getSiteAt(x, y + 1);
		}

		return adjacent;
	}

	/**
	 * return string with Site location and status: (x,y) --> STATUS
	 */
	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + ")" + " --> " + this.status;
	}

}
