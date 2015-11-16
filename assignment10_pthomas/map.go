package main

import (
    "fmt"
    "io/ioutil"
    "os"
	"regexp"
	"strings"
	"sync"
)

var wg sync.WaitGroup

func check(e error) {
    if e != nil {
        panic(e)
    }
}

func mapper(mapid int, filename string) {
	defer wg.Done()
	fmt.Println(filename)
    mapopdir := fmt.Sprintf("map_output/%d", mapid)
    os.Mkdir(mapopdir, 0777)

	files := make([]*os.File, 10)

	for i:=0; i < 10; i++ {
		f, err := os.Create(fmt.Sprintf("%s/%d", mapopdir, i))
		check(err)
		files[i] = f
	}

	dat, err := ioutil.ReadFile(filename)
    check(err)
	re := regexp.MustCompile("[^a-zA-Z0-9]")
	lines := strings.Split(string(dat), "\n")
	for _, line := range lines {
		words := strings.Split(line, " ")
		for _, word := range words {
			word = re.ReplaceAllString(word, "")
			if(len(word) > 0) {
				fIdx := word[0] % 10
				files[fIdx].WriteString(fmt.Sprintf("%s\t1\n",word))
			}
		}
	}

	for i:=0; i < 10; i++ {
		files[i].Sync()
		files[i].Close()
	}
}

func main() {

	dir := os.Args[1]
    files, _ := ioutil.ReadDir(dir)
	os.Mkdir("map_output", 0777)

    for i, f := range files {
		wg.Add(1)
		go mapper(i, fmt.Sprintf("input/%s", f.Name()))
    }

	wg.Wait()
}
