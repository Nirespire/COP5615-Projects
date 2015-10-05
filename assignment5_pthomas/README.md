# Assignment 5

Token ring algorithm simulation.

## Description

The program accepts the number of nodes as an argument. The program creates a ring of all nodes, and a token is passed around the ring. The main routine chooses 10 random nodes out of all in the ring. Each choosen node then randomly select a destination to send a message.

Once a message passes through the ring and reaches the source node again, it passes the token again.

## Output

The maximum time taken by a node, which decides to send a message, waits to encounter the token, send the message, and for the message to pass through the ring is the latency across such a network.

For an increasing number of nodes, the latency will increase. This can be see in the nodes_op.txt
