package main

/* 

 This is based on a similiar principle of DNS, where a large lookup is divided into a tree like structure.

 run using command: /usr/local/go/bin/go run name_lookup.go
 assuming that go is installed at /usr/local/go

 This code creates 26 channels to input names, for each alphabet(A-Z).
 It also creates 26 channels to direct queries to the respective part of the dictionary.

 This program only takes care of names in upper case.
*/

import(
  "bufio"
  "fmt"
  "os"
  "strings"
)

func check(e error) {
    if e != nil {
        panic(e)
    }
}

func alphabetChannel(id int, nameChannel <-chan byte, queryChannel <-chan string, output chan<- string) {
  dictionary := make(map[string]int)
  nameBuffer := make([]byte, 0)
  for {
    char, done := <- nameChannel
    if(char == '\n') {
      if (len(nameBuffer) > 0) {
        dictionary[string(nameBuffer)] = 1 
      }

      nameBuffer = nil
    } else {
      nameBuffer = append(nameBuffer, char)
    }
    if(!done) { break }
  }

  for {
    query := <- queryChannel
    if (dictionary[query] == 1) {
      output <- fmt.Sprintf("Found %s!\n", query)
    } else {
      output <- fmt.Sprintf("Not Found %s!\n", query)
    }  
    output <- "Enter name to be searched (enter \"exit\" to quit): "
  }
}

func printOutput(output <-chan string) {
  for {
    outputStr := <- output
    fmt.Print(outputStr)
  }
}

func main() {
  const (
    // 4K buffer size
    READ_BUF_SIZE = 4 * 1024
  )

  f, err := os.Open("census-derived-all-first.txt")
  check(err)

  var queryChannels [26]chan string
  var nameChannels [26]chan byte
  output := make(chan string)
  for i := range nameChannels {
    nameChannels[i] = make(chan byte)
    queryChannels[i] = make(chan string)
    go alphabetChannel(i, nameChannels[i], queryChannels[i], output)
  }

  go printOutput(output)

  r4 := bufio.NewReader(f)
  b4 := make([]byte, READ_BUF_SIZE)
  limit, err := r4.Read(b4)
  check(err)
  i := 0
  j := 0
  writeIdx := 0
  for ; i <= limit; i++ {
    if(i == READ_BUF_SIZE ) {
      i = 0
      climit, cerr := r4.Read(b4)
      check(cerr)
      limit = climit
    }

    nameChannels[writeIdx] <- b4[i]
    if (i == j) {
      writeIdx = int(b4[i]) - 65
    }

    if(b4[i] == '\n') {
      j = i + 1
    }

  }

  for i := range nameChannels {
    close(nameChannels[i])
  }

  reader := bufio.NewReader(os.Stdin)
  output <- "Enter name to be searched (enter \"exit\" to quit): "
  text, _ := reader.ReadString('\n')
  for ; text != "exit\n"; {
    queryIdx := text[0] - 65
    queryChannels[queryIdx] <- strings.Replace(text, "\n", "", -1)
    ctext, _ := reader.ReadString('\n')
    text = ctext
  }

  close(output)

  for i := range queryChannels {
    close(queryChannels[i])
  }
}
