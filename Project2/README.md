# Project 2

## Contributors
  1. Sanjay Nair
  2. Preethu Thomas
 
## What is Working
  
  - Can build 2D, 3D, imperfect 2D, imperfect 3D, line and full topologies
  - Can run gossip and push-sum algorithms on the network
  - Can receive feedback on how long the system took to set up and converge

## How to Run

- Execute 'sbt "run num_nodes topology algorithm"' with the following valid values for parameters:
    - num_nodes : The number of nodes you would like in the system
    - topology : The way the nodes should be connected to their neighbors
        - 3D : a cubic topology, with each central node having 6 neighbors (num_nodes will be rounded up to the nearest perfect cube)
        - 2D : a planar topology, with each central node having 4 neighbors (num_nodes will be rounded up to the nearest perfect square)
        - line : a 1D topology, with each central node having 2 neighbors
        - imp3D : similar to 3D, except each node with have one extra, random, neighbor (num_nodes will be rounded up to the nearest perfect cube)
        - imp2D : similar to 2D, except each node with have one extra, random, neighbor (num_nodes will be rounded up to the nearest perfect square)
        - full  : every node is neighbors with every other node in a full network
    - algorithm : how each node in the system should behave to provide some overall algorithm in the system
        - gossip : the master node will send 10 words, each to a random node. Once all the nodes have some initial gossip.
                   they will begin relaying that message to a random neighbor. Each node is responsible for counting and 
                    sending the word it receives to a random neighbor until it has seen each word it knows about 10 times 
                    (app.numGossipWords and app.gossipConvergenceNumber can be changed in application.conf). The node
                    will then signal that it has converged. The system continues till every node has converged.
        - push sum : Each node has "s" initialized to its id, and "w" initialized to 1.
                     A process push-sum message contains values (newS, newW) which needs to be added the existing (s,w) values of the node.
                     Once the (s,w) values are updates, half is kept with the node, the remaining is sent randomly to one of the neighbor nodes.
                     Each node keeps processing the (news, newW) message unless it has converged. A node is said to have converged if its (s/w) value remains
                     unchanged (up-to 10 decimal digits) while processing three consecutive push-sum messages.
                     The algorithm would have converged once all nodes have converged.
                     The master sends a start push-sum to one of the nodes in the topology. The start message is equivalent to (newS, newW) = (0,0) push-sum process message.
	
	- If you would like to run multiple tests automatically, reference the scripts folder that contains a bash
	script that can be configured to run the same algorithm and topology for multiple numbers of nodes.
	These scripts were used to automate collection of metrics for the report. NOTE: make sure the scripts are
	run from the same directory where build.sbt resides and can be run.

## Orientation

Wording used when referencing the virtual position of nodes in space.

   N
 W   E
   S

## 3D example with 27 nodes

----------------------------
            |   01 02 03
Top View    |   04 05 06
            |   07 08 09
----------------------------
            |   07 08 09
Front View  |   16 17 18
            |   25 26 27
----------------------------
            |   19 20 21
Bottom View |   22 23 24
            |   25 26 27
----------------------------

## Runtime for each algorithm, num_nodes, and topology

- The results of running the system with different parameters are compiled into the included report
- NOTE: the push-sum and gossip runtimes were recorded on two different computers. Although their performance
might not correspond well between algorithms, each algorithm's performance remains consistent with that computer's performance.
	Computer 1 (running push-sum): CPU: Intel Core i3-4030U @ 1.90GHz, 4GB RAM
	Computer 2 (running gossip): CPU: Intel Core i5-4670 @ 3.4GHz, 8GB RAM

## Largest Network For Each Topology (running on computer 2)

- Line : 20000
- 2D : 250000
- 3D : 103000
- Imperfect 2D : 205000
- Imperfect 3D : 102000
- Full : 1000