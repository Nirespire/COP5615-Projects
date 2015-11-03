package main

import (
    "crypto/tls"
    "fmt"
	"io"
    "log"
	"os"
	"sync"
)

var wg sync.WaitGroup

func client_thread(client_name string) {
	defer wg.Done()
	pem_file := fmt.Sprintf("certificates/%s.pem", client_name)
	key_file := fmt.Sprintf("certificates/%s.key", client_name)

	client_certificate, err := tls.LoadX509KeyPair(pem_file, key_file)
    if err != nil {
        log.Fatalf("client(%s) -- LoadCertificate -- %s", client_name, err)
	}

	log.Printf("client(%s) -- LoadCertificate -- Successful\n", client_name)

    client_config := tls.Config{Certificates: []tls.Certificate{client_certificate}, InsecureSkipVerify: true}
    client_connection, err := tls.Dial("tcp", "127.0.0.1:30000", &client_config)
    if err != nil {
        log.Fatalf("client(%s) -- connecting to server -- %s", client_name, err)
	}

    defer client_connection.Close()
	log.Printf("client(%s) -- connected to server -- %s\n", client_name, client_connection.RemoteAddr())

    message := fmt.Sprintf("Hi, my name is %s!", client_name)
    num_bytes, err := io.WriteString(client_connection, message)
    if err != nil {
        log.Fatalf("client(%s) -- write -- %s", client_name, err)
    }

    log.Printf("client(%s) -- write -- %q (%d bytes)\n", client_name, message, num_bytes)
    reply := make([]byte, 256)
    num_bytes, err = client_connection.Read(reply)
    log.Printf("client(%s) -- read  -- %q (%d bytes)\n", client_name, string(reply[:num_bytes]), num_bytes)
    log.Printf("client(%s) -- exiting --\n", client_name)
}

func main() {
    
	wg.Add(1)
	go client_thread(os.Args[1])
	wg.Wait()
}

