from general import *

class UKBWrapper:
	DICT_PATH = '../ukb-3.1/scripts/wn30_dict.txt'
	RELATIONS_PATH = '../ukb-3.1/scripts/wn30_rel.txt'
	UKB_DIR = '../ukb-3.1/'
	
	def __init__(self, corenlp_data, g):
		self.corenlp_data = corenlp_data
		self.g = g
		self.wnetid2lemma = {}
		self.relations = {}
		print(str(datetime.datetime.now()) + ' UKBWrapper built')
		
	def __load_wnet_dict(self):
		with open(self.DICT_PATH,'r') as f:
			lines = [line.rstrip('\n') for line in f]
			for line in lines:
				values = line.split()				
				for value in values[1:]:
					self.wnetid2lemma[value[0:10]] = values[0]			
	
	def __load_wnet_rel(self):
		with open(self.RELATIONS_PATH,'r') as f:
			lines = [line.rstrip('\n') for line in f]
			for line in lines:
				values = line.split()
				self.relations[values[0][3:] + values[1][3:]] = values[2][3:]
			
	def __contentWSD(self):
		for sentence in self.corenlp_data:
			tokens = self.corenlp_data[sentence]['tokens']
			index = 0
			content_str = ''
			word_id2token = {}
			
			#build input file for ukb
			for token in tokens:
				pos = Utils.mapPosTags(token['pos'])
				if pos == '':
					continue
				word = Utils.get_lemma(token['originalText'], pos)
				word_id = 'w' + str(index)
				index += 1
				content_str += word.lower() + '#' + pos + '#' + word_id + '#1 '				
				word_id2token[word_id] = token
				
			with open('../workdir/ukb_input.txt', 'w') as f:
				f.write("c1\n")
				f.write("%s\n" % content_str)
			
			#ukb running, result in workdir/ukb_output.txt
			ukb_wsd_command = [self.UKB_DIR + 'bin/ukb_wsd', '--ppr', '-K', self.UKB_DIR + 'scripts/wn30g.bin', '-D', self.UKB_DIR + 'scripts/wn30_dict.txt', '../workdir/ukb_input.txt']
			fo = open('../workdir/ukb_output.txt',"wb")
			subprocess.call(ukb_wsd_command, stdout=fo)
	
			#parsing result and graph update
			with open('../workdir/ukb_output.txt', 'r') as f:
				lines = [line.rstrip('\n') for line in f]
				for line in lines[1:]:
					values = line.split()
					
					word_id = values[1]
					concept_id = values[2]
					token = word_id2token[word_id]					
					ref = XPROJECT_OFFSET + str(token['characterOffsetBegin']) + '_' + str(token['characterOffsetEnd']) + '_' + token['originalText']
					
					self.g.add((rdflib.term.URIRef(ref), \
						   rdflib.term.URIRef(HAS_WSD_CONCEPT), \
						   rdflib.term.URIRef(UKB_CONCEPT + concept_id)))
						   
					self.g.add((rdflib.term.URIRef(ref), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
					))
					
					self.g.add((rdflib.term.URIRef(UKB_CONCEPT + concept_id), \
						   rdflib.term.URIRef(HAS_NAME), \
						   rdflib.term.Literal(self.wnetid2lemma[concept_id],  datatype=XSD.string)
					))
						   
					self.g.add((rdflib.term.URIRef(UKB_CONCEPT + concept_id), \
								OWL_TYPE, \
								UKB_CLASS_WSD_CONCEPT
					))
			#disk cleaning	
			os.remove('../workdir/ukb_input.txt')
			os.remove('../workdir/ukb_output.txt')

			
	def run(self):
		print(str(datetime.datetime.now()) + ' UKBWrapper running')
		self.__load_wnet_dict()
		self.__load_wnet_rel()
		self.__contentWSD()
		print(str(datetime.datetime.now()) +' UKBWrapper finished')
		
	def get_graph(self):
		return self.g
