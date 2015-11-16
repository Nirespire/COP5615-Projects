package main

import (
    "fmt"
    "io/ioutil"
    "os"
	"strings"
	"sync"
)

var wg sync.WaitGroup

func check(e error) {
    if e != nil {
        panic(e)
    }
}

func reducer(reduceid int, filename string) {
	defer wg.Done()
	fmt.Println(filename)
    reduceop := fmt.Sprintf("reduce_output/%d", reduceid)

	f, err := os.Create(reduceop)

	dat, err := ioutil.ReadFile(filename)
    check(err)
	prevword := ""
	wordcnt := 0
	lines := strings.Split(string(dat), "\n")
	for _, line := range lines {
		words := strings.Split(line, "\t")
		word := words[0]
		if(prevword == "") {
			prevword = word
		}

		if(prevword != word) {
			f.WriteString(fmt.Sprintf("%s\t%d\n",prevword,wordcnt))
			wordcnt = 0
			prevword = word
		}		

		wordcnt += 1
	}

	if(prevword != "") {
		f.WriteString(fmt.Sprintf("%s\t%d\n",prevword,wordcnt))
	}

	f.Sync()
	f.Close()
}

func main() {

    files, _ := ioutil.ReadDir("shuffle")
	os.Mkdir("reduce_output", 0777)

    for i, f := range files {
		wg.Add(1)
		go reducer(i, fmt.Sprintf("shuffle/%s", f.Name()))
    }

	wg.Wait()
}
