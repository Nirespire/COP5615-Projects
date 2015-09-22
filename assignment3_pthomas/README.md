
 This is based on a similiar principle of DNS, where a large lookup is divided into a tree like structure.

 run using command: /usr/local/go/bin/go run name_lookup.go
 assuming that go is installed at /usr/local/go

 This code creates 26 channels to input names, for each alphabet(A-Z).
 It also creates 26 channels to direct queries to the respective part of the dictionary.

 This program only takes care of names in upper case.

