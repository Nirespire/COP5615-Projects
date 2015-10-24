# Project 3

## Contributors

1. Sanjay Nair
2. Preethu Thomas


## Overview

This project implements a basic version of the Chord distributed hash table protocol using the Akka framework and
Actor model. The system simulates Chord joins, where nodes are able to dynamically join the system as well as lookup,
where any node on the Chord ring network could be queried for a key. This implementation takes advantage of the
theoretical efficiency of building the Chord ring and achieves logarithmic lookup performance as the number of nodes
increases linearly.

 
## What is Working

- Can build a Chord ring of n nodes within a 20 bit hash space using the Chord join algorithm
- Can have the nodes request x number of times into the system
- Will record the number of hops each request takes to be fulfilled and compute the average number of hops all the messages took to be fulfilled


## How to Run

- Execute ```sbt "run numNodes numRequests"``` where ```numNodes``` is the number of node
you would like in the system and ```numRequests``` is the number of requests each node must
send and get a result for before that node shuts down. The system shuts down when all nodes have shut down

- The program will output the average number of hops a message took to be delivered in the network

## Largest Network Run

262,144 nodes. Avg number of hops = 8


## Implementation Details

The Chord system that is being simulated has a 20 bit hash that can simulate a hash space of 2^20 unique values.
The values in the system are represented as integers ranging from 0 to 1048576.

When the program is first run, the system will begin to generate random values within the hash space and construct
Node objects according to those values. The nodes are inserted into the ring one at a time, each using a randomly chose
existing node as a "known node" to run the Chord node join algorithm. A node is joined into the system every 100 
milliseconds. 

Once it has been determined that the appropriate number of nodes have been created and joined the Chord
ring, every node is notified to begin querying for values. These values are also randomly generated within the hash 
space to simulate as if a random string of queries we being hashed into a key and sent to the node to be found in the
system. Every time a node receives a query, it checks to see if it is the appropriate node responsible for that value.
If it is, it will notify the original requester that the request has been fulfilled. If it is not, it will forward
the request to the appropriate node according to its finger table and increment the number of hops of the request by 
one. When a node receives a message that it's request has been completed, it will increment its count of fulfilled 
requests, and either request another random value after one second has elapsed, or notify the Manager that it is 
finished with its requests.

The Manager actor only exists to keep track of the number of nodes that have completed all their requests and compute
the final number of average hops each message had to make through the system to be fulfilled.



Please refer to report.pdf for additional analysis of performance.

