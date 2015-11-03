package main

import (
    "crypto/rand"
    "crypto/tls"
	"fmt"
	"io"
    "log"
    "net"
)

func main() {
 	server_name := "server"
	pem_file := fmt.Sprintf("certificates/%s.pem", server_name)
	key_file := fmt.Sprintf("certificates/%s.key", server_name)

	server_certificate, err := tls.LoadX509KeyPair(pem_file, key_file)
    if err != nil {
        log.Fatalf("server -- loadCertificate -- %s\n", err)
	}

	log.Printf("server -- loadCertificate -- Successful\n")

    server_config := tls.Config{Certificates: []tls.Certificate{server_certificate}, ClientAuth: tls.RequireAnyClientCert}
    server_config.Rand = rand.Reader
    server_ip := "0.0.0.0:30000"
    listener, err := tls.Listen("tcp", server_ip, &server_config)
    if err != nil {
        log.Fatalf("server -- listener -- %s\n", err)
    }

    log.Println("server -- listener -- Successful")
    for {
        server_connection, err := listener.Accept()

        if err != nil {
            log.Printf("server -- accept -- %s", err)
            break
        }

        log.Printf("server -- accept -- from %s\n", server_connection.RemoteAddr())
        go client_thread(server_connection)
    }
}

func client_thread(conn net.Conn) {
    defer conn.Close()
    tls, ok := conn.(*tls.Conn)
    if ok {
        err := tls.Handshake()
        if err != nil {
            log.Fatalf("server -- handshake -- failed: %s\n", err)
        }

        log.Println("server -- handshake -- Successful")

		server_message := "Hello, nice to meet you, my name is server"
        temp_buffer := make([]byte, 256)
        for {
            num_bytes_read, err := conn.Read(temp_buffer)
            if err != nil {
                log.Fatalf("server -- read -- %s\n", err)
            }

            log.Printf("server -- read --  %q\n", string(temp_buffer[:num_bytes_read]))

            num_bytes_write, err := io.WriteString(conn, server_message)
            log.Printf("server -- write --  %d bytes", num_bytes_write)
            if err != nil {
                log.Printf("server -- write -- %s", err)
                break
            }
        }
    }
    log.Println("server -- close -- ")
}
