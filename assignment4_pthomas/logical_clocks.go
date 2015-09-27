package main

import (
  "fmt"
  "os"
  "math/rand"
  "strconv"
)

type SyncMsg struct {
  from, msg int
}

func thread (id int, incr int, self <-chan SyncMsg, outs [2]chan<- SyncMsg, output chan<- string) {
  softClock := 0
  tabs := ""
  var msg SyncMsg
  msg.from = id
  if(incr == 8) {
    tabs = "\t"
  } else if(incr == 10) {
    tabs = "\t\t"
  }

  for {
    i := <-self
    if (i.from == -1) {
      softClock += (i.msg *incr)
      i := rand.Intn(2)
      if( i == 0 ) {
        output <-  fmt.Sprintf("%s<-%d", tabs, softClock)
      } else {
        output <-  fmt.Sprintf("%s%d->", tabs, softClock)
      }

      msg.msg = softClock
      outs[i] <- msg
    } else {
      output <-  fmt.Sprintf("%s(%d)", tabs, softClock)
      if(softClock < i.msg) {
        softClock = i.msg
        output <-  fmt.Sprintf("%s %d", tabs, softClock)
      }
    }
  }
}

func printOutput(output <-chan string) {
  for {
    i := <- output
    fmt.Printf("%s\n", i)
  }
}

func main() {
  if(len(os.Args) == 2) {
 	seed, _ := strconv.ParseInt(os.Args[1],0,64)
    rand.Seed(seed)
  }
  // http://stackoverflow.com/questions/2893004/how-to-allocate-array-of-channels-in-go
  var syncChans [5]chan SyncMsg
  for i := range syncChans {
    syncChans[i] = make(chan SyncMsg)
  }

  output := make(chan string)

  // creating three channels, where each channels increments its soft clock by 6,8, and 10 respectively.
  for i, j := 6, 0; i<=10; i,j = i+2, j+1 {

    // Provide link to the other two channels.
    var outputChannels [2]chan<- SyncMsg
    for  k1, k2 := (j+1) %3, 1 ;k1 != j; k1,k2 = (k1+1)%3, k2-1 {
      outputChannels[k2] = syncChans[k1]
    }

    go thread(j, i, syncChans[j], outputChannels, output)
  }

  go printOutput(output)
  
  // create 10 random events, each event will pick one of the channels,
  // send a message to that channel, by how many ticks to increments its soft clock,
  // and the receiving goroutine of that channel will message one of the other two channels.

  var sMsg SyncMsg
  sMsg.from = -1

  for i:= 0; i< 10; i+=1 {
    sMsg.msg = rand.Intn(10) + 1
    syncChans[rand.Intn(3)] <- sMsg
  }

  // close all syncChannels
  for i := range syncChans {
    close(syncChans[i])
  }

  //close output channel
  close(output)
}
