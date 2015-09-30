BEGIN { max = 0 }
/project2/ {
  node_no = $4 + 0
  top = $5
}
/Num/ {
  node_no =$3 + 0
}
/Setup/ {
  setup = $4
}
/Convergence/ {
  if($3 < 1) {
    op[top,node_no] =  log(1)
  } else {
    op[top,node_no] =  log($3)
  }
  if(node_no > max) { max = node_no }
  top_type[top] = 0
}

END{
  printf("num_of_nodes\t")
  for(t in top_type) {
    printf("%s\t",t)
  }  
  print ""
    
  for(i = 100; i<= max; i++) {
    line = ""

    for(t in top_type) {
        line = line op[t,i] "\t"
    }  
    
    if(length(line) > length(top_type)) {
      printf("%12d\t", i)
      print line
    }
  }
}
