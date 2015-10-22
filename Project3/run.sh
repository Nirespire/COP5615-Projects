
rm o.log 

for i in `seq 10 50000 524288`;
do
  sbt "run $i 10" >> o.log
  echo "$i" >> o.log
done 


grep -i -B 3 "all nodes done" o.log > result.log
