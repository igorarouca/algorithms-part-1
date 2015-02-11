/**
 * The <tt>Percolation<tt> class represents a data structure for simulating
 * percolation experiments on a N-by-N grid.
 * <p>
 * It supports operations to check if a <em>site<em> in a particular position 
 * of the percolation grid (row,column) is <em>open<em> or <em>full<em>.
 * A full site is an open site that can be connected to an open site in the 
 * top row of the grid via a chain of neighboring (left, right, up, down) open 
 * sites.
 * <p>
 * There are also operation to <em>open<em> a site in a give position and check
 * if the grid <em>percolates<em>. The grid percolates if there is a full site 
 * in the bottom row. In other words, filling all open sites connected to the 
 * top row guarantees that at least one open site on the bottom row will also 
 * be filled.
 * <p>
 * This percolation model relies on the class <code>WeightedQuickUnionUF</code>
 * to perform union-find operations on the percolation grid (i.e. connecting 
 * sites and finding sites that are connected.
 * 
 * 
 * @author Igor Arouca
 * @version 1.0
 * @since 2015-02-10
 */
public class Percolation {

	private Grid grid;

	// create N-by-N grid, with all sites blocked
	/**
     * Initializes an empty Percolation data structure with (N * N) sites.
     * All sites are initially BLOCKED.
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
		grid.getSiteAt(--i, --j).open();
	}

	/** Is site (i, j) open?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is open
	 */
	public boolean isOpen(int i, int j) {
		return grid.getSiteAt(--i, --j).isOpen();
	}

	/** Is site (i, j) full?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is full
	 */
	public boolean isFull(int i, int j) {
		return grid.getSiteAt(--i, --j).isFull();
	}

	/** Does the system percolate?
	 * The system percolates if there is a full site in the bottom row.
	 * 
	 * @return true if the system percolates
	 */
	public boolean percolates() {
		return grid.percolates();
	}

}

/**
 * The <tt>Grid</tt> class represents a 2-dimensional grid of sites. 
 * Each site is represented by an instance of <code>Site</code>.
 * Grid positions are zero-based, ranging from (0,0) to (n-1,n-1).
 * This implementation supports union-find operations on the grid's sites.
 * 
 * @author Igor Arouca
 * @version 1.0
 * @since 2015-02-10
 */
class Grid {

	private static final int VIRTUAL_TOP_SITE_INDEX = 0;

	private int size;		// number of sites
	private Site[][] sites;	// 2-dimensional grid of sites

	// Data structure to support union-find operations on the grid.
	// If there was an interface UnionFind, this object could be dynamically set
	private WeightedQuickUnionUF unionFind;

	/**
	 * Initializes a n-by-n grid with all sites initially blocked 
	 * 
	 * @param n the size of this 2-dimensional grid (n-by-n)
	 */
	Grid(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException(
				"Invalid grid size: " + n + 
				". Grid size (n) has to be greater than zero."
			);
		}

		this.size = n;

		initializeSites();
		initializeUnionFindStructure();
	}

	private void initializeSites() {
		this.sites = new Site[size][size];

		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				sites[i][j] = new Site(this, i, j);
			}
		}
	}

	private void initializeUnionFindStructure() {

		// creates a union-find structure with 2 extra nodes for virtual sites
		this.unionFind = new WeightedQuickUnionUF((size * size) + 2);

		createConnectionsToVirtualTopSite();
		createConnectionsToVirtualBottomSite();
	}

	private void createConnectionsToVirtualTopSite() {
		Site[] topRowSites = sites[0];

		for (Site site : topRowSites) {
			unionFind.union(
				getVirtualTopSiteUFIndex() , site.getUnionFindIndex()
			);
		}
	}

	int getVirtualTopSiteUFIndex() {
		return VIRTUAL_TOP_SITE_INDEX;
	}

	private void createConnectionsToVirtualBottomSite() {
		Site[] bottomRowSites = sites[size - 1];

		for (Site site : bottomRowSites) {
			unionFind.union(
				getVirtualBottomUFSiteIndex(), site.getUnionFindIndex()
			);
		}
	}

	private int getVirtualBottomUFSiteIndex() {
		return (size * size) + 1;
	}

	/**
	 * Get site at provided location (row,column)
	 * 
	 * @param row row index between 0 and (size - 1)
	 * @param column column index between 0 and (size - 1)
	 * @return site at location (row,column)
	 */	
	public Site getSiteAt(int row, int column) {
		return sites[row][column];
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

	/**
	 * Determines if the grid percolates by checking if the virtual top site
	 * is connected to the virtual bottom site.
	 * 
	 * @return true if the grid percolates or false otherwise
	 */
	public boolean percolates() {
		return unionFind.connected(
			getVirtualBottomUFSiteIndex(), getVirtualTopSiteUFIndex()
		);
	}

}

class Site {

	private static enum Status { BLOCKED, OPEN };

	private int row;
	private int column;

	private Grid grid;

	private Status status;

	Site(Grid grid, int row, int column) {
		if (row < 0 || row > grid.getSize()) {
			throw new ArrayIndexOutOfBoundsException(
				"row index is out of bounds: " + row
			);
		}

		if (column < 0 || column > grid.getSize()) {
			throw new ArrayIndexOutOfBoundsException(
				"column index is out of bounds: " + column
			);
		}

		this.row = row;
		this.column = column;

		this.grid = grid;

		this.status = Status.BLOCKED;
	}

	public boolean isFull() {
		return grid.getUnionFind().connected(
			grid.getVirtualTopSiteUFIndex(), this.getUnionFindIndex()
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

	public int getUnionFindIndex() {		
		return ((grid.getSize() * row) + column) + 1;
	}

	public Site getLeftAdjacentSite() {
		Site adjacent = null;

		if (isNotAtFirstColumn()) {
			adjacent = grid.getSiteAt(row, column - 1);
		}

		return adjacent;
	}

	private boolean isNotAtFirstColumn() {
		return column > 0;
	}

	public Site getRightAdjacentSite() {
		Site adjacent = null;

		if (isNotAtLastColumn()) {
			grid.getSiteAt(row, column + 1);
		}

		return adjacent;
	}

	private boolean isNotAtLastColumn() {
		return column < grid.getSize() - 1;
	}

	public Site getTopAdjacentSite() {
		Site adjacent = null;

		if (isNotAtFirstRow()) {
			adjacent = grid.getSiteAt(row - 1, column);
		}

		return adjacent;
	}

	private boolean isNotAtFirstRow() {
		return row > 0;
	}

	public Site getBottomAdjacentSite() {
		Site adjacent = null;

		if (isNotAtLastRow()) {
			adjacent = grid.getSiteAt(row + 1, column);
		}

		return adjacent;
	}

	private boolean isNotAtLastRow() {
		return row < grid.getSize() - 1;
	}

	/**
	 * return string with Site location and status: (row,column) --> STATUS
	 */
	@Override
	public String toString() {
		return "(" + this.row + "," + this.column + ")" + " --> " + this.status;
	}

}
