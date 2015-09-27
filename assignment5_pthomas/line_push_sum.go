/* Simpler version of Bitcoin mining project implemented in GO */
package main

import (
  "fmt"
  "math"
  "math/rand"
  "sync"
)

var wg sync.WaitGroup
var tenDigitConst float64 =  math.Pow(10, 10)

type PushSum struct {
  s float64
  w float64
  done int8
}

// https://gist.github.com/DavidVaini/10308388
func tenDigitRounded(pushSum PushSum) (rounded float64) {
  var round float64
  digit := pushSum.s/pushSum.w*tenDigitConst
  _, div := math.Modf(digit)
  if div >= 10 {
  	round = math.Ceil(digit)
  } else {
    round = math.Floor(digit)
  }

  rounded = round/tenDigitConst
  return
}

func startNode(id int, inputChannel <-chan PushSum, outputChannels [2]chan<- PushSum) {
  local := new(PushSum)
  local.s = float64(id)
  local.w = 1
  local.done = 0
  
  localSOverW := tenDigitRounded(*local)
  numOfNeighbors := len(outputChannels)
  for {
    newPushSum := <-inputChannel
    if(newPushSum.done == 10) {
      wg.Done()
    } else if(local.done < 3) {
      local.s = (local.s + newPushSum.s) / 2
      local.w = (local.w + newPushSum.w) / 2
      newSOverW := tenDigitRounded(*local)
      if(localSOverW == newSOverW) {
        local.done = local.done + 1
        if(local.done == 3) {
          fmt.Printf("id=%d,newS=%f,newW=%f,done=%d\n",id,local.s, local.w, local.done)
          newPushSum.done = newPushSum.done + 1
        }
      } else {
        localSOverW = newSOverW
        local.done =0
      }
      newPushSum.s = local.s
      newPushSum.w = local.w
      outputChannels[rand.Intn(numOfNeighbors)] <- newPushSum
    } else {
      outputChannels[rand.Intn(numOfNeighbors)] <- newPushSum
    }
  }
}

func main() {
  var lineChannels [10]chan PushSum
  for i := range lineChannels {
    lineChannels[i] = make(chan PushSum)
  }

  wg.Add(1)
  for i := 0; i <=9 ; i++ {
    var outputChannels [2]chan<- PushSum
    if i == 0  {
      outputChannels[0] =  lineChannels[i+1]
      outputChannels[1] =  lineChannels[i+1]
    } else if i == 9 {
      outputChannels[0] =  lineChannels[i-1]
      outputChannels[1] =  lineChannels[i-1]
    } else {
      outputChannels[0] =  lineChannels[i-1]
      outputChannels[1] =  lineChannels[i+1]
    }

    go startNode(i, lineChannels[i], outputChannels)
  } 

  start := new(PushSum)
  start.s = 0
  start.w = 0
  start.done = 0
  lineChannels[rand.Intn(len(lineChannels))] <- *start

  wg.Wait()

  for i := 0; i <=9 ; i++ {
    close(lineChannels[i])
  }
}
