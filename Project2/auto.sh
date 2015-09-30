for i in `cat imp2d_nodes.txt`;
do
  echo $i
  #sbt "run $i imp2D push-sum" >> imp2d.log
done

#awk -f log.awk imp2d.log 
