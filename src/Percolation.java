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

	private static final int VIRTUAL_TOP_SITE_INDEX = 0;

	private int n;		// number of sites
	private Site[][] sites;	// 2-dimensional grid of sites

	// Data structure to support union-find operations on the grid.
	// If there was an interface UnionFind, this object could be dynamically set
	private WeightedQuickUnionUF unionFind;

	/**
     * Initializes an empty Percolation data structure with (N * N) sites.
     * All sites are initially BLOCKED.
     * @throws java.lang.IllegalArgumentException if N <= 0
     * @param N the number of objects
     */
	public Percolation(int N) {
		if (N <= 0) {
			throw new IllegalArgumentException(
				"Invalid grid size: " + N + 
				". Grid size (n) has to be greater than zero.");
		}

		this.n = N;

		initializeSites();
		initializeUnionFindStructure();
	}

	private void initializeSites() {
		this.sites = new Site[n][n];

		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j)
				sites[i][j] = new Site(i, j);
	}

	private void initializeUnionFindStructure() {
		// creates a union-find structure with 2 extra nodes for virtual sites
		this.unionFind = new WeightedQuickUnionUF((n * n) + 2);

		createConnectionsToVirtualTopSite();
		createConnectionsToVirtualBottomSite();
	}

	private void createConnectionsToVirtualTopSite() {
		Site[] topRowSites = sites[0];

		for (Site site : topRowSites)
			unionFind.union(
				getVirtualTopSiteUFIndex() , site.getUnionFindIndex());
	}

	private int getVirtualTopSiteUFIndex() {
		return VIRTUAL_TOP_SITE_INDEX;
	}

	private void createConnectionsToVirtualBottomSite() {
		Site[] bottomRowSites = sites[n - 1];

		for (Site site : bottomRowSites)
			unionFind.union(
				getVirtualBottomUFSiteIndex(), site.getUnionFindIndex());
	}

	private int getVirtualBottomUFSiteIndex() {
		return (n * n) + 1;
	}

	/** Open site (i, j) if it is not open already
	 * 
	 * @param i
	 * @param j
	 */
	public void open(int i, int j) {
		sites[--i][--j].open();
	}

	/** Is site (i, j) open?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is open
	 */
	public boolean isOpen(int i, int j) {
		return sites[--i][--j].isOpen();
	}

	/** Is site (i, j) full?
	 * 
	 * @param i row index (starting at 1)
	 * @param j column index (starting at 1)
	 * @return true if the site is full
	 */
	public boolean isFull(int i, int j) {
		return sites[--i][--j].isFull();
	}

	/** Does the system percolate?
	 * The system percolates if there is a full site in the bottom row.
	 * 
	 * @return true if the system percolates
	 */
	public boolean percolates() {
		return unionFind.connected(
			getVirtualBottomUFSiteIndex(), getVirtualTopSiteUFIndex());
	}

	/**
	 * The <tt>Site<tt> class represents a site in the percolation grid.
	 * Each site instance has a defined position (row, column) in the grid 
	 * and a status: either OPEN or BLOCKED. Sites always start in the BLOCKED
	 * status, and can be opened using the <code>open</code> method.
	 * 
	 */
	class Site {

		/* Inner classes don't accept enum types, so the constants below
		 * are used to represent the possible statuses of a site
		 */
		
		private static final int BLOCKED = 0;
		private static final int OPEN = 1;

		private int row;						// row number from 0 to n-1
		private int column;						// column number from 0 to n-1

		private int status;						// status of the site

		Site(int row, int column) {
			if (row < 0 || row > n) {
				throw new ArrayIndexOutOfBoundsException(
					"row index is out of bounds: " + row
				);
			}

			if (column < 0 || column > n) {
				throw new ArrayIndexOutOfBoundsException(
					"column index is out of bounds: " + column
				);
			}

			this.row = row;
			this.column = column;

			this.status = BLOCKED;
		}

		/**
		 * Check if site is connected to one of the sites in the top row.
		 * 
		 * @return true if site is full and false otherwise
		 */
		boolean isFull() {
			return unionFind.connected(
				getVirtualTopSiteUFIndex(), this.getUnionFindIndex()
			);
		}

		boolean isOpen() {
			return status == OPEN;
		}

		/**
		 * Change site status to OPEN and connect site 
		 * to all open adjacent sites
		 */
		void open() {
			if (isOpen()) return;

			this.status = OPEN;
			connectToOpenAdjacentSites();
		}

		private void connectToOpenAdjacentSites() {
			connectTo(getTopAdjacentSite());
			connectTo(getRightAdjacentSite());
			connectTo(getBottomAdjacentSite());
			connectTo(getLeftAdjacentSite());
		}

		private void connectTo(Site adjacent) {
			if (adjacent == null || !adjacent.isOpen()) return;
	
			unionFind.union(getUnionFindIndex(), adjacent.getUnionFindIndex());
		}
	
		private int getUnionFindIndex() {		
			return ((n * row) + column) + 1;
		}

		Site getLeftAdjacentSite() {
			Site adjacent = null;

			if (isNotAtFirstColumn()) {
				adjacent = sites[row][column - 1];
			}

			return adjacent;
		}

		private boolean isNotAtFirstColumn() {
			return column > 0;
		}

		Site getRightAdjacentSite() {
			Site adjacent = null;

			if (isNotAtLastColumn()) {
				adjacent = sites[row][column + 1];
			}

			return adjacent;
		}

		private boolean isNotAtLastColumn() {
			return column < n - 1;
		}

		Site getTopAdjacentSite() {
			Site adjacent = null;

			if (isNotAtFirstRow()) {
				adjacent = sites[row - 1][column];
			}

			return adjacent;
		}

		private boolean isNotAtFirstRow() {
			return row > 0;
		}

		Site getBottomAdjacentSite() {
			Site adjacent = null;
	
			if (isNotAtLastRow()) {
				adjacent = sites[row + 1][column];
			}
	
			return adjacent;
		}
	
		private boolean isNotAtLastRow() {
			return row < n - 1;
		}

		/**
		 * return string with Site location and status: (row,column) --> STATUS
		 */
		@Override
		public String toString() {
			return "(" + this.row + "," + this.column + ")" + 
				" --> " + this.status;
		}
	}

}
