import java.io.*;
import java.util.*;
import java.lang.*;

// Data structure for a node in a linked list
class Item {
   int data;
   Item next;

   Item(int data, Item next) {
      this.data = data;
      this.next = next;
   }
}

// Data structure for representing a graph
class Graph {
   int n;  // # of nodes in the graph

   Item[] A; 
   // For u in [0..n), A[u] is the adjecency list for u

   Graph(int n) {
      // initialize a graph with n vertices and no edges
      this.n = n;
      A = new Item[n];
   }

   void addEdge(int u, int v) {
      // add an edge u -> v to the graph

      A[u] = new Item(v, A[u]);
   }
}

// Data structure holding data computed by DFS
class DFSInfo {
   int k; 
   // # of trees in DFS forest
   
   int[] T;
   // For u in [0..n), T[u] is initially 0, but when DFS discovers
   // u, T[u] is set to the index (which is in [1..k]) of the tree 
   // in DFS forest in which u belongs.

   int[] L;
   // List of nodes in order of decreasing finishing time

   int count;
   // initially set to n, and is decremented every time
   // DFS finishes with a node and is recorded in L

   DFSInfo(Graph graph) {
      int n = graph.n;
      k = 0;
      T = new int[n];
      L = new int[n];
      count = n-1;
   }
}


//main program

public class SCC {

   static void recDFS(int u, Graph graph, DFSInfo info) {
      // perform a recursive DFS, starting at u

      //k = tree # (starts at 1)
      //L = n (decrements every time we finish with a node and record it in L)

      //set node's tree equal to the current k
      info.T[u] = info.k;

      //for each intersection that is a successor of u:
      if (graph.A[u] != null && u != graph.A[u].data){
         int current = graph.A[u].data;
         Item next = graph.A[u].next;

         //check the first successor of u
         if (info.T[current] == 0){
            recDFS(current, graph, info);
         }
         //now check all of u's other successors
         while (next != null){
            current = next.data;
            if (info.T[current] == 0){
               recDFS(current, graph, info);
            }
            next = next.next;
         }
      }

      //record node in L[] using the count variable (since we have finished the node u)
      info.L[info.count] = u;

      //decrement the count variable
      info.count--;
   }

   static DFSInfo DFS(int[] order, Graph graph) {
        // performs a "full" DFS on given graph, processing 
        // nodes in the order specified (i.e., order[0], order[1], ...)
        // in the main loop.  

        int n = graph.n;    //number of nodes in given graph
        
        //declare a new DFSInfo object for the graph
        DFSInfo info = new DFSInfo(graph);

        //in DFSInfo, set all T to 0
        for (int i = 0; i < n; i++){
            info.T[i] = 0;
        }

        //count variable is already initialized in DFSInfo constructor to n

        //for each intersection in the graph, if it has no assigned tree (T[node] == 0), then do a recursive DFS on it
        //go through the nodes in the ORDER specified - provided by the int[] order input
        for (int j = 0; j < n; j++){
            if (info.T[order[j]] == 0){
               //increase the k variable by 1, since this node must be in a new component/tree
               info.k++;
               //call recDFS on the current node
               recDFS(order[j], graph, info);
            }
        }
        return info;
   }

   static boolean[] computeSafeNodes(Graph graph, DFSInfo info){
      // returns a boolean array indicating which nodes
      // are safe nodes.  The DFSInfo is that computed from the
      // second DFS.

      //create boolean array of size n
      boolean[] safeNodes = new boolean[graph.n];

      //set all safeNode values to false
      for (int a = 0; a < graph.n; a++){
         safeNodes[a] = false;
      }

      //create integer array of size info.k + 1
      //where each element outDegree[k] holds the outDegree of tree number k in the graph
      int[] outDegree = new int[info.k+1];

      //set all outdegrees to 0
      for (int b = 0; b < info.k+1; b++){
         outDegree[b] = 0;
      }

      //for each node of the graph, look at its outgoing edges
      //if the node has an edge that goes to another tree (another component), then the original node's tree's outgoing
      //degree is increment by 1
      int u;
      for (int i = 0; i < graph.n; i++){
         u = i;
            //for each intersection v that is a "succcessor" of u, check if u and v are in the same tree
            if (graph.A[u] != null){
                int v = graph.A[u].data;
                Item next = graph.A[u].next;

                //do the u's first edge
                //if the edge ventures out of u's tree, increment u's tree's outgoing degree by 1
                if (info.T[u] != info.T[v]){
                   outDegree[info.T[u]] += 1;
                }

                //do the other edges
                //if the edge ventures out of u's tree, increment u's tree's outgoing degree by 1
                while (next != null){
                    v = next.data;
                    if (info.T[u] != info.T[v]){
                     outDegree[info.T[u]] += 1;
                    }
                    next = next.next;
                }
            }
      }

      //now, determine which nodes are safe
      //each node that lies in a sink component (component outdegree = 0) is considered safe
      for (int c = 0; c < graph.n; c++){
         if (outDegree[info.T[c]] == 0){
            safeNodes[c] = true;
         }
      }

      return safeNodes;
   }

    static Graph reverse(Graph graph){
      
        //create the reverse graph object, with the same number of intersections as the input graph
        Graph reverse = new Graph(graph.n);

        int u;

        //for each node in input graph, look at its outgoing edges and add the reverse of each edge to the reverse graph
        for (int i = 0; i < graph.n; i++){
            u = i;

            //for each intersection v that is a "succcessor" of u, add edge v --> u to reverse graph
            //that is, there is now a one-way street from v --> u in reverse graph
            if (graph.A[u] != null){
                int v = graph.A[u].data;
                Item next = graph.A[u].next;

                //do the u's first edge
                reverse.addEdge(v, u);

                //do the other edges
                while (next != null){
                    v = next.data;
                    reverse.addEdge(v, u);
                    next = next.next;
                }
            }
        }
        return reverse;
   }

   //declare BufferedWriter object
   static BufferedWriter output;

   public static void main(String[] args) throws IOException, NullPointerException{

    //create buffered writer object to read input and write output
    output = new BufferedWriter(new OutputStreamWriter(System.out, "ASCII"), 4096);

    //pass the path to the file as a parameter
    File file = new File("C:\\Users\\laure\\Documents\\NYU\\2020 FALL\\Basic Algorithms\\PA5\\mytest0.txt");

    int currentLine = 1;    //integer to hold the number of the current line
    String inputLine;       //string to hold the current line's information
    int intersections;      //integer to hold the number of intersections in the map/graph
    int streets;           //integer to hold the number of one-way streets in the map/graph
    int start;              //integer to hold the node number that one-way street starts at
    int end;                //integer to hold the node number that the one-way street ends at

    Scanner sc = new Scanner(file);     //Scanner object to read the input file

    inputLine = sc.nextLine();    //store first line in inputLine variable

    
    String[] graphInfo = inputLine.split(" ");      //split first input line
    intersections = Integer.parseInt(graphInfo[0]);         //store number of intersections in map (nodes)
    streets = Integer.parseInt(graphInfo[1]);      //store number of one-way streets in map (edges)

    //create new graph with correct number of intersections
    Graph newYorkCity = new Graph(intersections);

    currentLine++;    //increment current line by 1 (now it is 2)
    
    //number of total lines in input file
    int totalLines = streets + 1;

    while (currentLine <= totalLines){
       inputLine = sc.nextLine();
       String[] passageInfo = inputLine.split(" ");    //split current input line
       start = Integer.parseInt(passageInfo[0]);       //store the start node of the passage
       end = Integer.parseInt(passageInfo[1]);         //store the end node of the passage

       //add new edge to graph here, from start node to end node (stored above)
       newYorkCity.addEdge(start, end);

       currentLine++; //increment line counter by 1
    }

    //create the reverse graph
    Graph reverseNYC = reverse(newYorkCity);

    //create order array for processing the nodes in the reverse graph [0.....n)
    int[] order = new int[reverseNYC.n];
    //set all numbers equal to their indices because
    //for the reverse graph, we process the nodes in order 0,...,n
    for (int j = 0; j < reverseNYC.n; j++){
        order[j] = j;
    }

    //call DFS on reverse graph
    DFSInfo reverseInfo = DFS(order, reverseNYC);

    //call DFS on original graph with L from previous DFS as input
    int[] reverseL = reverseInfo.L;
    DFSInfo finalInfo = DFS(reverseL, newYorkCity);

    //create boolean array to store all of the safe nodes that we get from computeSafeNodes()
    boolean[] safeNodes = computeSafeNodes(newYorkCity, finalInfo);
    
    //print out each safe node
    for (int d = 0; d < newYorkCity.n; d++){
       if (safeNodes[d]){
          output.write(d + " ");
       }
    }

    output.write("\n");

    //flush and close bufferedWriter object
    output.flush();
    output.close();

   }
}
