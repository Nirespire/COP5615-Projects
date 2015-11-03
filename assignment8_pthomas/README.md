# Assignment 8

GoLang TLS example, using x509 certificates created using "openssl req" command.

## Description

Using openssl, the certificates were created.

These certificates were used to establish a TLS connection between the server and the user, and then communicate between them.


## Output

### Creating certificates

```
[preethu@32-laptop assignment8_pthomas]$ bash create_certificates.sh server
Creating certificates/server.pem
Generating a 2048 bit RSA private key
...................+++
..................................................+++
writing new private key to 'certificates/server.key'
-----
[preethu@32-laptop assignment8_pthomas]$ bash create_certificates.sh pthomas
Creating certificates/pthomas.pem
Generating a 2048 bit RSA private key
..........................................................................................................................................................................+++
........................+++
writing new private key to 'certificates/pthomas.key'
-----
```

### Server

```
[preethu@32-laptop assignment8_pthomas]$ /usr/local/go/bin/go run server.go
2015/11/02 23:50:10 server -- loadCertificate -- Successful
2015/11/02 23:50:10 server -- listener -- Successful
2015/11/02 23:50:14 server -- accept -- from 127.0.0.1:58943
2015/11/02 23:50:14 server -- handshake -- Successful
2015/11/02 23:50:14 server -- read --  "Hi, my name is pthomas!"
2015/11/02 23:50:14 server -- write --  42 bytes
2015/11/02 23:50:14 server -- read -- EOF
exit status 1
[preethu@32-laptop assignment8_pthomas]$
```

### Client

```
[preethu@32-laptop assignment8_pthomas]$ /usr/local/go/bin/go run  client.go pthomas
2015/11/02 23:50:14 client(pthomas) -- LoadCertificate -- Successful
2015/11/02 23:50:14 client(pthomas) -- connected to server -- 127.0.0.1:30000
2015/11/02 23:50:14 client(pthomas) -- write -- "Hi, my name is pthomas!" (23 bytes)
2015/11/02 23:50:14 client(pthomas) -- read  -- "Hello, nice to meet you, my name is server" (42 bytes)
2015/11/02 23:50:14 client(pthomas) -- exiting --
[preethu@32-laptop assignment8_pthomas]$ 
```

## References

1. CERTIFICATE CREATION : https://www.openssl.org/docs/manmaster/apps/req.html
2. TLS Dial example : https://golang.org/pkg/crypto/tls/#example_Dial
