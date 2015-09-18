
/* Simpler version of Bitcoin mining project implemented in GO */
package main

import (
  "crypto/sha256"
  "fmt"
  "sync"
)

var wg sync.WaitGroup

func bitcoinMine(output chan<- string, prefix string) {
  defer wg.Done()

  for k1 := int8(32); k1 < 127; k1++ {
    for k2 := int8(32); k2 < 127; k2++ {
      for k3 := int8(32); k3 < 127; k3++ {
        t := prefix + fmt.Sprintf("%c%c%c", k1, k2, k3)
        byteArray := []byte(t)
        hash := fmt.Sprintf("%x", sha256.Sum256(byteArray))
        i:= 0
        for ; hash[i] == '0' && i < len(hash); i++ {
        }

        if( i == 10) {
          output <- fmt.Sprintf("%s\t%s", t, hash)
        }
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
  prefix := "pthomas"
  output := make(chan string)

  for k1 := int8(32); k1 < 127; k1++ {
    for k2 := int8(32); k2 < 127; k2++ {
      for k3 := int8(32); k3 < 127; k3++ {
        t := prefix + fmt.Sprintf("%c%c%c", k1, k2, k3)
        wg.Add(1)
        go bitcoinMine(output, t)
      }
    }
  }

  go printOutput(output)
  wg.Wait()
  close(output)
}
