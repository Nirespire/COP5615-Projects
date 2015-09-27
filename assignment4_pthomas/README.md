#Assignment 4

##Title
Lampard Logical clock algorithm Simulation.

##Description
Three goroutines are created which has its own soft clock, channel and increment number (the value by which the soft clock increments in that routine) are created to illustrate three thread/processes each with its own soft clock.
The main thread randomly chooses one of the three, and send it a random number between 1 to 10; the random number indicates the number of increments of that thread's soft clock.
Once the goroutine receive the number of times it increases its softclock, it randomly send a message to the other two its updated softclock.
Once a goroutine receive an updated clock message, it checks that value against its own softclock, and updates its own softclock if  the message was greater than its own. 

##Output
<pre>
[preethu@32-laptop ~]$ /usr/local/go/bin/go run logical_clocks.go 10
        40->
                (0)
                 40
60->
        80->
                (40)
                 80
        (80)
114->
        (80)
<-156
186->
         114
        (114)
                (80)
                 156
                <-196
192->
         186
                <-216
        (186)
         196
        (196)
         216
        (216)
[preethu@32-laptop ~]$ /usr/local/go/bin/go run logical_clocks.go 3
                <-90
        (0)
60->
                <-100
         90
        (90)
         100
        (100)
<-108
                (100)
                 108
        <-116
(108)
 116
<-140
        156->
        <-188
                (108)
                 156
                (156)
                <-176
(140)
 188
        (188)
[preethu@32-laptop ~]$
</pre>
