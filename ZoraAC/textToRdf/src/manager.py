from general import *
from corenlp_wrapper import *
from semafor_wrapper import *
from ukb_wrapper import *
import networkx as nx
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import math
import shutil


class Manager:
	TOOL_NAME = 'XPROJECT'

	def __init__(self, argv):
		self.argv = argv
		self.text = ''
		self.output_file = 'xproject_out'
		self.format = 'xml'
		self.g = rdflib.Graph()
		self.path = None
		self.web_input = False
		print('\n' + self.TOOL_NAME + ' Start')
		
	def __text_preprocessing(self):
		self.text = self.text.replace('\n', ' ')
		
	def __build_workspace(self):
		if not os.path.exists('../workdir'):
			os.makedirs('../workdir')
		#if not os.path.exists('../results'):
			#os.makedirs('../results')
		
	def __bind_properties(self):
		object_properties = [IS_FRAME_ON, HAS_FRAME_ELEMENT,  EVOKE_FRAME, HAS_DEPENDENCY, HAS_ENTITY_TYPE, HAS_COREF, IS_COREF, \
			 IS_COMPOSED_BY, HAS_WSD_CONCEPT]
		
		for property in object_properties:
			self.g.add((rdflib.URIRef(property),
					OWL_TYPE,
					OWL_OBJECT_PROPERTY ))
					
		datatype_properties = [HAS_POS, BEGIN, END, HAS_NAME]
		
		for property in datatype_properties:
			self.g.add((rdflib.URIRef(property),
					OWL_TYPE,
					OWL_DATATYPE_PROPERTY))
		
		#bind properties to classes	
		self.g.add((rdflib.URIRef(EVOKE_FRAME), OWL_DOMAIN, SEMAFOR_CLASS_FRAME_ELEMENT))
		self.g.add((rdflib.URIRef(HAS_FRAME_ELEMENT), OWL_DOMAIN, SEMAFOR_CLASS_FRAME_ELEMENT))
		self.g.add((rdflib.URIRef(HAS_DEPENDENCY), OWL_DOMAIN, XPROJECT_CLASS_WORD))
		self.g.add((rdflib.URIRef(HAS_POS), OWL_DOMAIN, XPROJECT_CLASS_WORD))
		self.g.add((rdflib.URIRef(HAS_ENTITY_TYPE), OWL_DOMAIN, XPROJECT_CLASS_WORD))
		self.g.add((rdflib.URIRef(HAS_COREF), OWL_DOMAIN, XPROJECT_CLASS_WORD))
		self.g.add((rdflib.URIRef(IS_COREF), OWL_DOMAIN, XPROJECT_CLASS_WORD))
		self.g.add((rdflib.URIRef(BEGIN), OWL_DOMAIN, XPROJECT_CLASS_WORD))	
		self.g.add((rdflib.URIRef(END), OWL_DOMAIN, XPROJECT_CLASS_WORD))	
		self.g.add((rdflib.URIRef(IS_COMPOSED_BY), OWL_DOMAIN, XPROJECT_CLASS_WORD))	
		self.g.add((rdflib.URIRef(HAS_WSD_CONCEPT), OWL_DOMAIN, XPROJECT_CLASS_WORD))	
		self.g.add((rdflib.URIRef(HAS_NAME), OWL_DOMAIN, UKB_CLASS_WSD_CONCEPT))	
		
		
	def __add_owl_classes(self):
		
		word_properties = [IS_FRAME_ON, EVOKE_FRAME, HAS_DEPENDENCY, HAS_POS, HAS_ENTITY_TYPE, HAS_COREF, IS_COREF, \
			 IS_COMPOSED_BY, HAS_WSD_CONCEPT, HAS_NAME]
	
		##add classes
		self.g.add((XPROJECT_CLASS_WORD, OWL_TYPE, OWL_CLASS))
		self.g.add((UKB_CLASS_WSD_CONCEPT, OWL_TYPE, OWL_CLASS))
		self.g.add((XPROJECT_CLASS_COMPOUND_WORD, OWL_TYPE, OWL_CLASS))
		self.g.add((SEMAFOR_CLASS_FRAME, OWL_TYPE, OWL_CLASS))
		self.g.add((SEMAFOR_CLASS_FRAME_ELEMENT, OWL_TYPE, OWL_CLASS))
		
		#add classes relations
		self.g.add((XPROJECT_CLASS_COMPOUND_WORD, OWL_SUB_CLASS_OF, XPROJECT_CLASS_WORD))
		
		"""for property in word_properties:
			self.g.add((rdflib.URIRef(property), 
						OWL_DOMAIN, 
						XPROJECT_CLASS_WORD
			))"""
			
		
		
	def __remove_duplicate_triples(self):
		tmp_g = self.g
		self.g = rdflib.Graph()		
		for s,p,o in tmp_g:
			if (s,p,o) not in self.g:
				self.g.add((s,p,o))	

	def __save_result(self):
		name_result_file = '../workdir/result_' + str(os.getpid())
			
		if self.format == 'triples':
			name_result_file = name_result_file + '.txt'	
			with open(name_result_file, 'w') as f:
				for s,p,o in self.g:
					f.write('(' + s.n3() + ' ' + p.n3() +  o.n3() + ')\n')
		elif self.format == 'image':
			self.__draw()
		else:	
			name_result_file = name_result_file + '.txt'	
			with open(name_result_file, 'w') as f:
				f.write(self.g.serialize(destination=None, format=self.format).decode())
		
	def pipeline(self):
		print(self.TOOL_NAME + ' Running')
		self.__text_preprocessing()
		self.__build_workspace()

		coreNLP_instance = CoreNLPWrapper(self.text, self.g)		
		coreNLP_instance.run()
		self.g = coreNLP_instance.get_graph()
		coreNLP_instance.close()
		
		
		#semafor_instance = SemaforWrapper(coreNLP_instance.get_corenlp_data(), self.g)
		#semafor_instance.run()
		#self.g = semafor_instance.get_graph()
		
		#ukb_instance = UKBWrapper(coreNLP_instance.get_corenlp_data(), self.g)
		#ukb_instance.run()
		#self.g = ukb_instance.get_graph()		
		
		self.__bind_properties()
		self.__add_owl_classes()
		self.__remove_duplicate_triples()
		
		if self.web_input:	
			if self.format == 'image':
				self.__draw()
			else:
				r = self.__save_result()
		return
		
	
	def __draw(self):
		filename = '../workdir/result_' + str(os.getpid())
		nxg = nx.DiGraph()
		label2node = {}
		node2label = {}
		edge2label = {}
		plt.figure(figsize=(50, 50))
		
		i=0
		#node identification
		for s,p,o in self.g:
			if '#' in s.n3().decode():
				s_label =  s.n3().decode().split('#')[1][:-1]
			else:
				s_label = s.n3().decode()
			
			if 'XMLSchema#integer>' in o.n3().decode():
				o_label = o.n3().decode().split('#')[1][:-1] + ':' + o.n3().decode().split('^^')[0]
			elif '#' in o.n3().decode():
				o_label =  o.n3().decode().split('#')[1][:-1]
			else:
				o_label = o.n3().decode()
			
			if s_label not in label2node:
				label2node[s_label] = i
				node2label[i] = s_label
				i += 1
			if o_label not in label2node:
				label2node[o_label] = i
				node2label[i] = o_label
				i += 1
				
		#graph building
		for s,p,o in self.g:
			if '#' in s.n3().decode():
				s_label =  s.n3().decode().split('#')[1][:-1]
			else:
				s_label = s.n3().decode()
			if 'XMLSchema#integer>' in o.n3().decode():
				o_label = o.n3().decode().split('#')[1][:-1] + ':' + o.n3().decode().split('^^')[0]
			elif '#' in o.n3().decode():
				o_label =  o.n3().decode().split('#')[1][:-1]
			else:
				o_label = o.n3().decode()
			p_label =  p.n3().decode().split('#')[1][:-1]
			nxg.add_node(label2node[s_label])
			nxg.add_node(label2node[o_label])
			nxg.add_edge(label2node[s_label], label2node[o_label])
			edge2label[(label2node[s_label], label2node[o_label])] = p_label
		
		#print('NODES', [x for x in nxg.nodes()])
		#print('EDGES', [x for x in nxg.edges()])
		
		pos = nx.spring_layout(nxg, k=10/math.sqrt(nxg.order()))
		#pos = nx.random_layout(nxg)
		#pos = nx.circular_layout(nxg)
		
		nx.draw_networkx(nxg, pos, node_size=8000, cmap=plt.cm.YlGn, labels=node2label, vmin=0.2, node_color=range(len(nxg.nodes())))
		nx.draw_networkx_edge_labels(nxg, pos, edge_labels=edge2label, alpha=0.5, arrowsize=20,font_size=16, label_pos=0.5)

		plt.axis('off')
		plt.savefig(filename + '.png') # save as png
		
		

	def manage_input(self):	
		print(self.TOOL_NAME + ' Reading input')
		
		if len(self.argv) <= 1:
			print("No param has been provided. Please see: python xproject.py --help")
			exit(1)
		else:
			i = 1
			while i < len(self.argv):
				if self.argv[i] == '--text' or self.argv[i] == '-t':
					self.text = self.argv[i + 1]
					i += 2
				elif self.argv[i] == '--output-file-name' or self.argv[i] == '-o':
					self.output_file = self.argv[i + 1]
					i += 2
				elif self.argv[i] == '--format' or self.argv[i] == '-f':
					if self.argv[i + 1] in ['xml', 'rdf/xml', 'n3', 'turtle', 'pretty-xml', 'png', 'triples', 'image']:
						if self.argv[i + 1] == 'rdf/xml':
							self.format = 'xml'
						else:
							self.format = self.argv[i + 1]
					else:
						print("ERROR <format_type>: # " + str(self.argv[i + 1]) + " # Format must be one of: xml, n3, turtle, pretty-xml, png\nPlease see: python xproject.py --help")		
						exit(1)
					i += 2
				elif self.argv[i] == '--input-file-name' or self.argv[i] == '-i':
					if os.path.isfile(self.argv[i + 1]):
						with open(self.argv[i + 1], 'r') as f:
							self.text = f.read()
						i += 2
					else:
						print('ERROR File not found: ' + self.argv[i + 1])
						exit(1)
				
				elif self.argv[i] == '--web' or self.argv[i] == '-w':
					if self.argv[i + 1] == "true":
						self.web_input = True					
						i += 2
				
				elif self.argv[i] == '--path' or self.argv[i] == '-p':
					if os.path.exists(self.argv[i + 1]):
						self.path = self.argv[i + 1]
						i += 2
					else:
						print('ERROR The directory ' + self.argv[i + 1] + ' does not exist')
						exit(1)
						
				elif self.argv[i] == '--help' or self.argv[i] == '-h':
					print("\n#####################################\nRelease 1.0 xproject\n")
					print("Usage: python xproject -t <input_text> \n LIST OF PARAMS:\n" \
					" -t, --text \t\t\t<input_text> \t\tMANDATORY \ttext to be parsed. <input_text> must be enclosed between \" \"\n" \
					" -i, --input-file-name \t\t<input_file_name> \tOPTIONAL \tread a text from a file. It substitutes -t or --text option\n" \
					" -o, --output-file-name \t<output_file_name> \tOPTIONAL \tredirect output on a file\n" \
					" -f, --format \t\t\t<format type> \t\tOPTIONAL \t<format_type> must be one of: xml, rdf/xml, n3, turtle, pretty-xml, png. DEFAULT: xml.\n\t\t\t\t\t\t\t\t\tIn case of png the xml file will be created as well\n" \
					" -p, --path \t\t\t<path> \t\t\tOPTIONAL \tposition in the file system of files with results. DEFAULT DIRECTORY: results/\n" \
					" -w, --web\n"\
					" -h, --help \t\t\t-\t\t\tOPTIONAL \tshow this mesagge\n" )
					exit(1)
				else:
					print("\nERROR Param: # " + str(self.argv[i]) + " # Please see: python xproject.py --help")
					exit(1)
		
	def get_r(self):
		return self.g.serialize(destination=None, format=self.format).decode()				
		
	
	
	def show(self):	
		print(self.TOOL_NAME + ' Preparing output')
		
		if not self.path:
			self.path = '../results/'
		
		if self.path[-1] != '/':
			self.path += '/'
		
		if self.format == 'png':
			self.g.serialize(destination=self.path + self.output_file, format='xml')
			args = ['python', 'rdf2dot.py', self.path + self.output_file]
			process = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			stdout, stderr = process.communicate()
			process.wait()
			dot_string = stdout.decode()
			s = gv.Source(dot_string, filename=self.path + self.output_file, format="png")	
			s.render(self.path + self.output_file + ".png", view=False)
			print('\nOUTPUT#\nOutput redirected in: ' + './' + self.path + self.output_file + self.format + ', The image has been saved in ./' + self.path + self.output_file + '.png')
		
		else:
			if self.output_file != 'xproject_out':				
				self.g.serialize(destination=self.path + self.output_file + '.' + self.format, format=self.format)	
				print('\nOUTPUT#\nOutput redirected in: ' + './' + self.path + self.output_file)
			else:
				print('\nOUTPUT#\n' + self.g.serialize(destination=None, format=self.format).decode())
					
