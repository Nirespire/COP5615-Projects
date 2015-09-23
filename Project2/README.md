# Project 2

## Contributors
  1. Sanjay Nair
  2. Preethu Thomas
  

## Concepts
 
  


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
        - push sum : 


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

## Project Questions

NA