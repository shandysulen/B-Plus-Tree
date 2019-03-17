package Project;

import java.lang.*;
import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

public class bplustree {
	int m;
	int numNodes;
	int numPairs;
	InternalNode root;
	LeafNode tempRoot;

	/*~~~~~~~~~~~~~~~~ HELPER FUNCTIONS ~~~~~~~~~~~~~~~~*/

	/**
	 * This method performs a standard binary search on a sorted
	 * ArrayList<DictionaryPair> and returns the index of the dictionary pair
	 * with target key t if found. Otherwise, this method returns -1.
	 * @param dps: list of dictionary pairs sorted by key within leaf node
	 * @param t: target value being searched for
	 * @return index of the target value if found, else -1
	 */
	private int binarySearch(ArrayList<DictionaryPair> dps, int t) {
		Comparator<DictionaryPair> c = new Comparator<DictionaryPair>() {
			@Override
			public int compare(DictionaryPair o1, DictionaryPair o2) {
				Integer a = Integer.valueOf(o1.key);
				Integer b = Integer.valueOf(o2.key);
				return a.compareTo(b);
			}
		};
		return Collections.binarySearch(dps, new DictionaryPair(t, 0), c);
	}

	private int getMidpoint() {
		return (int)Math.ceil(this.m / 2.0) - 1;
	}

	private ArrayList<DictionaryPair> logicalSort(ArrayList<DictionaryPair> dictionary, DictionaryPair newDP) {
		ArrayList<DictionaryPair> allSortedPairs = dictionary;
		allSortedPairs.add(newDP);
		Collections.sort(allSortedPairs);
		return allSortedPairs;
	}

	private void splitInternalNode(InternalNode in) {
		if (this.root == in) {

			// Split keys and pointers in half
			int midpoint = getMidpoint();
			List<Integer> newRootKey = Arrays.asList(in.keys.get(midpoint));
			List<Integer> halfKeys1 = in.keys.subList(0, midpoint);
			List<Integer> halfKeys2 = in.keys.subList(midpoint + 1, in.keys.size());
			List<Node> halfPointers1 = in.childPointers.subList(0, midpoint + 1);
			List<Node> halfPointers2 = in.childPointers.subList(midpoint + 1, in.childPointers.size());

			// Update current root with half of keys and half of pointers
			in.keys = new ArrayList<Integer>(halfKeys1);
			in.childPointers = new ArrayList<Node>(halfPointers1);

			// Create new sibling internal node and add half of keys and pointers
			InternalNode rightSibling = new InternalNode(this.m, new ArrayList<Integer>(halfKeys2));
			rightSibling.childPointers = new ArrayList<Node>(halfPointers2);

			// Create new root node and add midpoint key and pointers
			InternalNode newRoot = new InternalNode(this.m, new ArrayList<Integer>(newRootKey));
			newRoot.addChildPointer(in);
			newRoot.addChildPointer(rightSibling);
			this.root = newRoot;

		}
	}

	/*~~~~~~~~~~~~~~~~ API: DELETE, INSERT, SEARCH ~~~~~~~~~~~~~~~~*/

	public void delete(int key) {
		if (numNodes == 0) {
			System.err.println("Invalid operation: The B+ tree is currently empty.");
		} else if (numNodes == 1) {
			int keyIndex = binarySearch(this.tempRoot.dictionary, key);
			if (keyIndex < 0) {
				double value = this.tempRoot.dictionary.get(keyIndex).value;
				this.tempRoot.dictionary.remove(keyIndex);
				System.out.println("Successfully deleted (" + key + ", " + value + ")");
			} else {
				System.err.println("Invalid operation: Key not found within B+ tree.");
			}
		} else {
			/* This flow of execution is for any B+ trees >= 2 nodes */

			// Start at root and find way down
			InternalNode currNode = this.root;
			int i;

			while (true) {
				// Iterate through keys in internal node to find index of appropriate pointer
				for (i = 0; i < currNode.keys.size(); i++) {
					if (key <= currNode.keys.get(i)) {
						break;
					}
				}

				// Break out if we have found index of pointer to leaf node
				Node nextNode = currNode.childPointers.get(i);
				if (nextNode instanceof LeafNode) {
					break;
				} else {
					currNode = (InternalNode)nextNode;
				}
			}

			// Get leaf node with found index
			LeafNode ln = (LeafNode)currNode.childPointers.get(i);

			// Find index of target dictionary pair
			int keyIndex = binarySearch(ln.dictionary, key);

			// Delete dictionary pair from leaf node
			ln.dictionary.remove(keyIndex);

			if (ln.isDeficient()) {

				// First, try to borrow from left then right sibling
				if (!(ln.leftSibling == null) && (ln.leftSibling.isLendable())) {
//					DictionaryPair borrowedDP = ln.leftSibling.dictionary.get(0);
//					ArrayList<DictionaryPair> updatedDict = Arrays.asList(borrowedDP)
//					ln.dictionary.(borrowedDP);
				}
			}
		}
	}

	public void insert(int key, double value){
		if (numNodes == 0) {

			// Create leaf node as first node in B plus tree (root is null)
			LeafNode ln = new LeafNode(new DictionaryPair(key, value), m);

			// Increment counters
			numNodes++;
			numPairs++;

			// Set as temporary leaf node root
			this.tempRoot = ln;

		} else if (this.root == null) {
			/* Flow of execution goes here when there is only a single leaf node
			within the B+ tree. */

			LeafNode ln = this.tempRoot;

			// Insert root leaf node fails if node becomes overfull
			if (!ln.insert(new DictionaryPair(key, value))) {

				// Sort all the dictionary pairs with the included pair to be inserted
				ArrayList<DictionaryPair> allPairs = logicalSort(ln.dictionary, new DictionaryPair(key, value));

				// Split the sorted pairs into two halves
				int midpoint = getMidpoint();
				List<DictionaryPair> halfDict1 = ln.dictionary.subList(0, midpoint);
				List<DictionaryPair> halfDict2 = ln.dictionary.subList(midpoint, ln.dictionary.size());

				// Create internal node to serve as parent, use dictionary midpoint key
				List<Integer> parent_keys = Arrays.asList((halfDict2.get(0)).key);
				InternalNode parent = new InternalNode(this.m, new ArrayList<Integer>(parent_keys));

				// Update leaf node's dictionary to be one half of what it is
				ln.updateDictionary(halfDict1);

				// Update leaf node's parent pointer
				ln.setParent(parent);

				// Create new LeafNode that holds the other half
				LeafNode newLeafNode = new LeafNode(halfDict2, this.m, parent);

				// Update child pointers of parent node
				parent.addChildPointer(ln);
				parent.addChildPointer(newLeafNode);

				// Increment counters & update variables
				this.numNodes += 2;

				// Set new internal parent node as root
				this.root = parent;

			}

			// A pair insert always happens in this flow of execution
			this.numPairs++;

		} else {

			/* Height of B+ tree is guaranteed to be >= 2, so we start at
			 * the root node and traverse down the tree to find the appropriate
			 * node to insert into.
			 */

			// No need for tempRoot anymore
			this.tempRoot = null;

			// Search for appropriate LeafNode
			LeafNode ln = search(key, this.root);

			// Sort all the dictionary pairs with the included pair to be inserted
			ArrayList<DictionaryPair> allPairs = logicalSort(ln.dictionary, new DictionaryPair(key, value));

			// Split the sorted pairs into two halves
			int midpoint = getMidpoint();
			List<DictionaryPair> halfDict1 = ln.dictionary.subList(0, midpoint);
			List<DictionaryPair> halfDict2 = ln.dictionary.subList(midpoint, ln.dictionary.size());

			// Update leaf node's dictionary to be one half of what it is
			ln.updateDictionary(halfDict1);

			// Create new LeafNode that holds the other half
			LeafNode newLeafNode = new LeafNode(halfDict2, this.m, ln.parent);

			// Add new key to parent for proper indexing
			int newParentKey = halfDict2.get(0).key;
			ln.parent.keys.add(newParentKey);

			// Add new pointer that routes to newLeafNode object
			ln.parent.addChildPointer(newLeafNode);

			// Record current root for later counter management
			InternalNode oldRoot = this.root;

			/* If parent is overfull, repeat the process up the tree,
			   until no deficiencies are found */
			InternalNode in = ln.parent;
			while (in != null) {
				if (in.isOverfull()) {
					splitInternalNode(in);
				} else {
					break;
				}
				in = in.parent;
			}

			// Increment counters and variables

			if (this.root == oldRoot) {
				numNodes++;
			} else {
				/* If root changed from before the splitting of the internal
				node, then we increase by two because splitting at root creates
				2 nodes instead of one. */
				numNodes += 2;
			}

			numPairs++;

		}
	}

	public LeafNode search(int key, Node node) {

		// Initialize keys and index variable
		ArrayList<Integer> keys = ((InternalNode)node).keys;
		int i;

		// Find next node on path to appropriate leaf node
		for (i = 0; i < keys.size(); i++) {
			if (key < keys.get(i)) { break; }
		}

		/* Return node if it is a LeafNode object,
		   otherwise repeat the search function a level down */
		Node childNode = ((InternalNode)node).childPointers.get(i);
		if (childNode instanceof LeafNode) {
			return (LeafNode)childNode;
		} else {
			return search(key, ((InternalNode)node).childPointers.get(i));
		}
	}

	public bplustree(int m) {
		this.m = m;
		this.numNodes = 0;
		this.numPairs = 0;
		this.root = null;

		System.out.println("B Plus tree successfully initialized.");
	}

	public class Node {
		InternalNode parent;
	}

	public class InternalNode extends Node {
		int maxDegree;
		int minDegree;
		ArrayList<Integer> keys;
		ArrayList<Node> childPointers;

		public boolean isOverfull() {
			return this.keys.size() == this.maxDegree;
		}

		public void addChildPointer(Node pointer) {
			this.childPointers.add(pointer);
		}

		public InternalNode(int m, ArrayList<Integer> keys) {
			this.maxDegree = m;
			this.minDegree = (int)Math.ceil(m/2.0);
			this.keys = keys;
			this.childPointers = new ArrayList<Node>();
		}

	}

	public class LeafNode extends Node {
		int maxNumPairs;
		int minNumPairs;
		LeafNode leftSibling;
		LeafNode rightSibling;
		ArrayList<DictionaryPair> dictionary;

		public void setParent(InternalNode parent) {
			this.parent = parent;
		}

		public boolean isDeficient() {
			return this.dictionary.size() == this.minNumPairs;
		}

		public boolean isFull() {
			return this.dictionary.size() == this.maxNumPairs;
		}

		public boolean isLendable() {
			return this.dictionary.size() > this.minNumPairs;
		}

		public boolean insert(DictionaryPair dp) {

			if (this.isFull()) {
				System.out.println("Full node encountered while inserting. Insert completed via leaf node split");
				return false;
			} else {
				this.dictionary.add(dp);
				return true;
			}
		}

		public void updateDictionary(List<DictionaryPair> dps) {
			this.dictionary = new ArrayList<>(dps);
		}

		/**
		 * Constructor that creates a new LeafNode and performs
		 * an initial DictionaryPair insert. Used to create initial root node.
		 * @param dp: first dictionary pair insert into new node
		 * @param m: upper bound on degree
		 */
		public LeafNode(DictionaryPair dp, int m) {
			this.maxNumPairs = m - 1;
			this.minNumPairs = (int)Math.ceil(m/2.0) - 1;
			this.dictionary = new ArrayList<DictionaryPair>();
			this.insert(dp); // may need to be sorted
		}

		/**
		 * Constructor that creates a new LeafNode object in the event of an overfull LeafNode.
		 * @param dps: List of Dictionary Pair objects to be immediately inserted into new LeafNode
		 * @param m: upper bound on degree
		 * @param parent: parent of newly created child LeafNode
		 */
		public LeafNode(List<DictionaryPair> dps, int m, InternalNode parent) {
			this.maxNumPairs = m - 1;
			this.parent = parent;
			this.dictionary = new ArrayList<DictionaryPair>(dps);
		}

	}

	public class DictionaryPair implements Comparable<DictionaryPair> {
		int key;
		double value;

		public DictionaryPair(int key, double value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(DictionaryPair o) {
			if (key == o.key) { return 0; }
			else if (key > o.key) { return 1; }
			else { return -1; }
		}
	}

	public static void main(String[] args) {

		// Ensure correct number of arguments
		if (args.length != 1) {
			System.err.println("usage: java bplustree <file_name>");
			System.exit(-1);
		}

		// Read from file
		String file_name = args[0];
		try {
			File file = new File(System.getProperty("user.dir") + "/" + file_name);
			Scanner sc = new Scanner(file);
			bplustree bpt = null;
			while (sc.hasNextLine()) {
				String line = sc.nextLine().replace(" ", "");
				System.out.print(line);
				String[] tokens = line.split("[(,)]");
				System.out.println(" -> " + Arrays.toString(tokens));

				switch (tokens[0]) {
					case "Initialize":
						bpt = new bplustree(Integer.parseInt(tokens[1]));
						break;
					case "Insert":
						bpt.insert(Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]));
						System.out.println("Successfully inserted (" + tokens[1] + ", " + tokens[2] + ").");
						break;
					case "Delete":
						bpt.delete(Integer.parseInt(tokens[1]));
						break;
					case "Search":
						// Search with tokens[1] (and maybe tokens[2]?)
						break;
					default:
						throw new IllegalArgumentException("\"" + tokens[0] + "\"" +
								" is an unacceptable input.");
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IllegalArgumentException e) {
			System.err.println(e);
		}
	}
}