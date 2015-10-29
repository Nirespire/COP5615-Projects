
rm o.log 

for i in `seq 10 1000 10000`;
do
  sbt "run $i 10" >> o.log
  echo "$i" >> o.log
done 


grep -i -B 3 "Avg num hops for all Nodes" o.log > result.log
