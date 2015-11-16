# Assignment 8

A simulation of how mapreduce steps are executed, this example show wordcount using 10 reducers.

## Description

### Placing the file on Distributed FS

First the file is split into 60KB files and placed in input folder, this is to represent the splitting on files on DFS

A map thread is called on each split of the file, this emits a [key, value] output.

Based on the key, the output is sent to one of the reducers.

The files from each mapper is sent to a reduce machine, known as the shuffle step, and each of these inputs to the reducer is sorted.

The reducer uses the sorted output to find the final word count.

## Output

### Output Files

#### Map output : map_output
#### Intermediate output : shuffle
#### Reduce output : reduce_output

### Running the simulation
```
[preethu@32-laptop assignment10_pthomas]$ bash mapreduce.sh 
input/mapinputaj
input/mapinputaa
input/mapinputab
input/mapinputaf
input/mapinputac
input/mapinputad
input/mapinputag
input/mapinputae
input/mapinputah
input/mapinputai
shuffle/9
shuffle/0
shuffle/1
shuffle/2
shuffle/3
shuffle/4
shuffle/5
shuffle/7
shuffle/8
shuffle/6
[preethu@32-laptop assignment10_pthomas]$ 
```

## References

1. input text file : http://www.gutenberg.org/ebooks/1661
2. golang read file : https://gobyexample.com/reading-files
3. golang writing files : https://gobyexample.com/writing-files
4. golang regex : https://golang.org/pkg/regexp/
