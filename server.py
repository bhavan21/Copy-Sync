#!/usr/bin/python           # This is server.py file

import os
import socket               # Import socket module
import thread
import json
import MySQLdb
# import keys


def new_client_conn(c,addr):
	# try:
	while True:
		try:
		   msg = c.recv(20240)
		   print ("Recieved: "+msg)
		   text =msg.replace("\r","\\\r").replace("\\","\\\\").replace("\"","\\\"")
		   print(text)
		   os.system("printf \""+text+"\" | xclip -selection c")
		   c.send("ok\n")
		except:
			c.close()
			print "connection closed!"
			break


s = socket.socket()         # Create a socket object
host = "192.168.0.108" # Get local machine name
port = 1997        # Reserve a port for your service.
s.bind((host, port))        # Bind to the port
print host
s.listen(5)                 # Now wait for client connection.
while True:
	c, addr = s.accept()     # Establish connection with client.
	print 'Got connection from', addr
	try:
		thread.start_new_thread( new_client_conn, (c,addr) )
	except:
		print "Error: unable to start thread"
                 