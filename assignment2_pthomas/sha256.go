
/* Simpler version of Bitcoin mining project implemented in GO */
package main

import (
  "bytes"
  "crypto/sha256"
  "fmt"
  "sync"
)

var wg sync.WaitGroup

func bitcoinMine(output chan<- string, prefix string) {
  defer wg.Done()

  for k1 := int8(32); k1 < 127; k1++ {
    for k2 := int8(32); k2 < 127; k2++ {
      t := prefix + fmt.Sprintf("%c%c", k1,k2)
      byteArray := []byte(t)
      hash := fmt.Sprintf("%x", sha256.Sum256(byteArray))
      i:= 0
      for ; hash[i] == '0' && i < len(hash); i++ {
      }
 
      if( i == 3) {
        output <- fmt.Sprintf("%s\t%s", t, hash)
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
  baseStr := make([]int8, 0)
  baseStr = append(baseStr, 32)

  for j := 0; j < 1; {
    wg.Add(1)
    var buffer bytes.Buffer
    buffer.WriteString(prefix)
    for i:= 0; i < len(baseStr); i++ {
      buffer.WriteString(fmt.Sprintf("%c", baseStr[i]))
    }

    go bitcoinMine(output, buffer.String())
    if(baseStr[0] < 126) {
      baseStr[0]++
    } else {
      j++
    }
  }

  go printOutput(output)
  wg.Wait()
  close(output)
}
