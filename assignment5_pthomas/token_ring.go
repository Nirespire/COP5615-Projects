package main

import (
  "fmt"
  "math/rand"
  "os"
  "time"
  "strconv"
  "sync"
)

type SyncMsg struct {
  from, to int64
}

var wg sync.WaitGroup

func thread (id int64, read <-chan SyncMsg, write chan<- SyncMsg, nodes int64, output chan<- time.Duration) {
  var token SyncMsg
  var msg SyncMsg
  token.from, msg.from = -2, id
  
  idx, sendMsgCnt := 0, 0
  startBuf := make([]time.Time, 0)

  for {
    i := <- read
    switch {
      case (i.from == -1) :
        sendMsgCnt +=1
        startBuf = append(startBuf, time.Now())
        fmt.Printf("Node %d needs to sent msg!\n", id)
      case (i.from == id) :
        elapsed := time.Since(startBuf[idx])
        idx += 1
        if (idx == len(startBuf)) {
          idx, startBuf = 0, make([]time.Time, 0)
        }
        output <- elapsed
        write <- token
        fmt.Printf("Node %d got back its own msg!\n", id)
      case (i.from == -2 && sendMsgCnt == 0) :
        write <- token
      case (i.from == -2 && sendMsgCnt > 0) :
        sendMsgCnt -=1
        msg.to = rand.Int63n(nodes)
        if (msg.to == id) { msg.to = (msg.to - 1) % nodes}
        write <- msg
      case (i.to == id) :
        fmt.Printf("Node %d received msg from Node %d!\n", id, i.from)
         write <-i
      default :
        write <- i   
    }
  }
}

func printOutput(output chan time.Duration, syncChans []chan SyncMsg, nodes int64) {

  defer wg.Done()
  counter := 1
  max := time.Since(time.Now())
  for {
    i, done := <- output
    counter += 1
    if (!done) { break}

    if (i > max) {
        max = i
    }

    if(counter == 10) {
      fmt.Printf("Max latency time for %d nodes :  %s\n", nodes, max)
      close(output)
    }
  }

  //stop token

  // close all syncChannels
  for i := range syncChans {
    close(syncChans[i])
  }
}

func main() {
	os.Stderr = os.Stdout
  var token SyncMsg
  var sMsg SyncMsg
  sMsg.from, token.from = -1, -2

  nodes := int64(10)
  if(len(os.Args) == 2) {
 	seed, _ := strconv.ParseInt(os.Args[1], 0, 64)
    nodes = seed
  }

  syncChans := make([]chan SyncMsg,0)
  for i := int64(0); i < nodes; i= i+1 {
    syncChans = append(syncChans, make(chan SyncMsg))
  }

  output := make(chan time.Duration)

  // creating three channels, where each channels increments its soft clock by 6,8, and 10 respectively.
  for i, j := int64(0), int64(1) ; i < nodes; i,j = i+1, ((j+1) % nodes) {

    // Provide link to the other two channels.
    go thread(i, syncChans[i], syncChans[j], nodes, output)
  }

  wg.Add(1)
  go printOutput(output, syncChans, nodes)

  syncChans[0] <- token

  // create 10 random events, each event will pick one of the channels,
  // send a message to that channel, by how many ticks to increments its soft clock,
  // and the receiving goroutine of that channel will message one of the other two channels.


  for i:= 0; i< 10; i+=1 {
    syncChans[rand.Int63n(nodes)] <- sMsg
  }
  
  wg.Wait()
}
