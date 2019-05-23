from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse
import subprocess
from bs4 import BeautifulSoup, NavigableString, Tag
import sys, traceback
import html
import urllib
import os
import cgi
import datetime
import time
import base64
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from socketserver import ThreadingMixIn
import threading

#glab.sc.unica.it:10000 -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "input_text=Danilo is a good guy&output_format=rdf/xml&mode=1"
INDEX_PATH = 'web/index.html'

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


class HTTPServer_RequestHandler(BaseHTTPRequestHandler):
	NUMBER_OF_REQUEST = 0
	
	def do_POST(self):
		result = ''
		try:
			form = cgi.FieldStorage(
				fp=self.rfile,
				headers=self.headers,
				environ={'REQUEST_METHOD':'POST',
						 'CONTENT_TYPE':self.headers['Content-Type'],
						 })
			if 'input_text' in form and form['input_text'].value != '':	
				text = form['input_text'].value
				output_format = form['output_format'].value
				
				print('TEXT:', text)
				print('OUTPUT_FORMAT:', output_format)
			
			
				command = ['python', 'xproject.py', \
						'-t', text, \
						'-f', output_format, \
						'-w', 'true'] 
				p = subprocess.Popen(command)
				out, err = p.communicate()
				p.wait()
				print(err)
	
				try:		
					
					
					"""if output_format == 'image':
						self.send_response(200)
						self.send_header('Content-type', 'image/png')
						self.end_headers()
						print('READING IMAGE', '../workdir/result_' + str(p.pid) + '.png')
						
						with open('../workdir/result_' + str(p.pid) + '.png', 'rb') as f:
							content = base64.b64encode(f.read())
						
						print(type(content))
						print(type('data:image/png;base64,' + content.decode("utf-8")))
						print(type(bytes('data:image/png;base64,' + content.decode("utf-8"), 'utf-8')))
						self.wfile.write(bytes('data:image/png;base64,' + content.decode("utf-8"), 'utf-8'))	
						os.remove('../workdir/result_' + str(p.pid) + '.png')
						print(str(datetime.datetime.now()) + ' Result sent')
						return
					else:"""	
						
					self.send_response(200)
					self.send_header('Content-type','text/html; charset=UTF-8;'	)
					self.end_headers()
					result = ''
					with open('../workdir/result_' + str(p.pid) + '.txt', 'r') as fr:
						result = fr.read()
						
					if 'mode' not in form or ('mode' in form and form['mode'].value != '1'):
						result = result.replace('<', '&lt;')
					os.remove('../workdir/result_' + str(p.pid) + '.txt')	
					self.wfile.write(bytes(result,'utf-8'))	
					print(str(datetime.datetime.now()) + ' Result sent')
					return			
				
				except:
					traceback.print_exc(file=sys.stdout)
					self.wfile.write(bytes( 'ERROR: An error occurs during the elaboration. Please try again later.','utf-8'))	
					return
			else:
					self.send_response(200)
					self.send_header('Content-type','text/html; charset=UTF-8;'	)
					self.end_headers()
					self.wfile.write(bytes( 'ERROR: Text can\'t be empty.','utf-8'))
			
			
		except:
			print('Exception raised, load empty page')
			traceback.print_exc(file=sys.stdout)
			with open(INDEX_PATH, 'r') as fhtml:
				parsed_html = BeautifulSoup(fhtml.read())
				self.wfile.write(bytes( 'ERROR: An error occurs during the elaboration. Please try again later.','utf-8'))		
		return
		
	def do_GET(self):
		self.send_response(200)
		self.send_header('Content-type','text/html; charset=UTF-8'	)
		self.end_headers()
		with open(INDEX_PATH, 'r') as fhtml:
			parsed_html = BeautifulSoup(fhtml.read())
			self.wfile.write(bytes(str(parsed_html),'utf-8'))
		return		
 
def run():
	
	if not os.path.exists('./src/workdir'):
		os.makedirs('./src/workdir')

	server_address = ('0.0.0.0', 10000)
	"""httpd = HTTPServer(server_address, HTTPServer_RequestHandler)
	print('running server...')
	httpd.serve_forever()"""
	
	print('starting server...')
	server = ThreadedHTTPServer(server_address, HTTPServer_RequestHandler)
	print('running server... use <Ctrl-C> to stop')
	server.serve_forever()
 
 
run()