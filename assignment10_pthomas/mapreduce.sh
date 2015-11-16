#!/bin/bash

#simulate making a folder on hdfs
mkdir input shuffle

#simulate putting file on hdfs
split --bytes=60000 pg1661.txt mapinput
mv mapinput* input

#simulate map part of mapreduce
/usr/local/go/bin/go run map.go input

#simulate shuffle, sort part of mapreduce
for i in `seq 0 9`;
do 
	sort map_output/*/$i > shuffle/$i
done

#simulate reduce part of 
/usr/local/go/bin/go run reduce.go
