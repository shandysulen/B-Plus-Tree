# B+ Tree

B+ trees offer significant value by providing efficient data retrieval in block-oriented storage applications like file systems and databases. A B+ tree is nothing more than a tree (with a particularly high fanout or order, which we shall refer to as _m_) that satisfies the following conditions:

* An internal node (nodes that aren't the root or leaves) must have a number of children _d_ such that &#8968;_m_/2&#8969;	&#8804; _d_ &#8804; _m_
* The root node may have at least 2 children and up to _m_ children
* The number of keys _k_ that an internal node may carry is &#8968;_m_/2&#8969; - 1	&#8804; _k_ &#8804; _m_ - 1 
* Leaf nodes have no children, but the number of dictionary pairs _k_ that a single leaf node may carry is &#8968;_m_/2&#8969; - 1	&#8804; _k_ &#8804; _m_ - 1

A B+ tree is similar to a B tree except that all the dictionary pairs lie in the leaf nodes.

## Getting Started

This program was developed, compiled, run, and tested only with [Java Development Kit 1.8.0_201 (Java 8)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). To compile the program, run the following command:

`$ javac bplustree.java`

To execute the program, run the following command:

`$ java bplustree <input_file>`

## Running

For the program to work properly, ensure that the _input_file_ follows the format:

* The first line must contain _Initialize(m)_ where _m_ is the order of the B+ tree
* All subsequent lines may contain only the following operations: 
    1. Insert(_key_, _value_) where _key_ is an integer and _value_ is a floating point number
    2. Delete(_key_) where _key_ is the key of the dictionary pair you would like to delete
    3. Search(_key_) where _key_ is the key of the dictionary pair whose value you would like to find
    4. Search(_lowerBound_, _upperBound_) where all values of dictionary pairs whose keys fall within _lowerBound_ &#8804; _key_ &#8804; _upperBound_ will be recorded

Output for each search query will be recorded within a file titled __output_file.txt__.

### Sample Input File

```
Initialize(3)
Insert(21, 0.3534)
Insert(108, 31.907)
Insert(56089, 3.26)
Insert(234, 121.56)
Insert(4325, -109.23)
Delete (108)
Search(234)
Insert(102, 39.56)
Insert(65, -3.95)
Delete (102)
Delete (21)
Insert(106, -3.91)
Insert(23, 3.55)
Search(23, 99)
Insert(32, 0.02)
Insert(220, 3.55)
Delete (234)
Search(65)
```

### Sample Output File

```
121.56
3.55, -3.95
-3.95
```

## Authors
* **[Shandy Sulen](https://github.com/shandysulen)**
