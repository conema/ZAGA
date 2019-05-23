from general import *
import os.path
import sys, traceback

class SemaforWrapper:
		
	SEMAFOR_DIR = '../semafor-master/'
	
	def __init__(self, corenlp_data, g):
		self.g = g
		self.corenlp_data = corenlp_data
		self.semafor_out_list = []
		print(str(datetime.datetime.now()) + ' SemaforWrapper built')
	
	def __call_semafor(self):
		
		for sentence in self.corenlp_data:
			s = ''
			for token in self.corenlp_data[sentence]['tokens']:
				s += token['before'] + token['originalText'] + token['after']
			with open('../workdir/' + str(os.getpid()) + 'temp_sentences.txt', 'a') as f:
				f.write("%s\n" % s)
		semafor_command = [self.SEMAFOR_DIR + 'bin/runSemafor.sh', '../workdir/' + str(os.getpid()) + 'temp_sentences.txt',  '../workdir/' + str(os.getpid()) + 'temp_out.rdf', '4']
		subprocess.call(semafor_command, stdout=open(os.devnull, 'w'), stderr=open(os.devnull, 'w'))
		#subprocess.call(semafor_command)
		self.__load_semafor_output('../workdir/' + str(os.getpid()) + 'temp_out.rdf')
			
		if os.path.isfile("../workdir/" + str(os.getpid()) + "temp_sentences.txt"):
			os.remove("../workdir/" + str(os.getpid()) + "temp_sentences.txt")
		if os.path.isfile("../workdir/" + str(os.getpid()) + "temp_out.rdf"):
			os.remove("../workdir/" + str(os.getpid()) + "temp_out.rdf")
		
		
	def __load_semafor_output(self, filename):
		with open(filename, 'r') as f:
			for line in f.readlines():
				self.semafor_out_list += [json.loads(line)]				
		
	def __parse_semafor(self):		
		sentence_lenght = {}					
		s_count = 0
		for semafor_out in self.semafor_out_list:	   
		
			text_tokens = semafor_out['tokens']
			text_tokens_corenlp = self.corenlp_data[s_count]['tokens']
			
			for frame in semafor_out['frames']:
				target_name = frame['target']['name']
				target_spans = frame['target']['spans']
				offset_text_reference_frame = ''
				for span in target_spans:
					
					offset = Utils.get_offset(text_tokens_corenlp, int(span['start']) + 1, int(span['end']) + 1)
					offset_text = span['text']
					offset_text_reference_frame = XPROJECT_OFFSET + offset + '_' + offset_text.replace(' ', '_')
					#print(text_tokens_corenlp, int(span['start']), int(span['end']))
					#print(text_tokens)
					#print(offset_text_reference)
		
					self.g.add((rdflib.term.URIRef(offset_text_reference_frame), \
						   rdflib.term.URIRef(EVOKE_FRAME), \
						   rdflib.term.URIRef(FRAME + target_name)
					))				
		
					if span['start'] < span['end'] - 1:
						#text composed by more words
						self.g.add((rdflib.term.URIRef(offset_text_reference_frame), \
								OWL_TYPE, \
								XPROJECT_CLASS_COMPOUND_WORD
						))
		
						for i in range(span['start'], span['end']):
							offset_word = Utils.get_offset(text_tokens_corenlp, i + 1, i + 2)
							offset_text_word = text_tokens[i]
							offset_text_reference_word = XPROJECT_OFFSET + offset_word + '_' + offset_text_word
		
							self.g.add((rdflib.term.URIRef(offset_text_reference_frame), \
								   rdflib.term.URIRef(IS_COMPOSED_BY), \
								   rdflib.term.URIRef(offset_text_reference_word)
							))
								   
							self.g.add((rdflib.term.URIRef(offset_text_reference_frame), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
							))
					else:
						#text composed by a single word
						self.g.add((rdflib.term.URIRef(offset_text_reference_frame), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
						))
		
				annotation_sets = frame['annotationSets']
				for set in annotation_sets:
					frame_elements = set['frameElements']
					for frame_element in frame_elements:
						frame_element_name = frame_element['name']
						frame_element_spans = frame_element['spans']
						for span in frame_element_spans:
							offset = Utils.get_offset(text_tokens_corenlp, int(span['start']) + 1, int(span['end']) + 1)
							offset_text = span['text']
							offset_text_reference = XPROJECT_OFFSET + offset + '_' + offset_text.replace(' ', '_')
							
							
							self.g.add((rdflib.term.URIRef(FRAME_ELEMENT + frame_element_name), \
								   rdflib.term.URIRef(IS_FRAME_ON), \
								   rdflib.term.URIRef(offset_text_reference)
							))
							
							self.g.add((rdflib.term.URIRef(FRAME + target_name), \
								   rdflib.term.URIRef(HAS_FRAME_ELEMENT), \
								   rdflib.term.URIRef(FRAME_ELEMENT + frame_element_name)
							))							
								   
							self.g.add((rdflib.term.URIRef(offset_text_reference), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
							))
		
							if span['start'] < span['end'] - 1:
		
								for i in range(span['start'], span['end']):
									offset_word = Utils.get_offset(text_tokens_corenlp, i + 1, i + 2)
									offset_text_word = text_tokens[i]
									offset_text_reference_word = XPROJECT_OFFSET + offset_word + '_' + offset_text_word
		
									self.g.add((rdflib.term.URIRef(offset_text_reference), \
										   rdflib.term.URIRef(IS_COMPOSED_BY), \
										   rdflib.term.URIRef(offset_text_reference_word)
									))
									
									self.g.add((rdflib.term.URIRef(offset_text_reference), \
											OWL_TYPE, \
											XPROJECT_CLASS_WORD
									))
		
									#print((rdflib.term.URIRef('http://' + offset_text_reference), \
									#	   rdflib.term.URIRef(IS_COMPOSED_BY), \
									#	   rdflib.term.URIRef('http://' + offset_text_reference_word)))
			s_count += 1
										   
	def run(self):
		print(str(datetime.datetime.now()) + ' SemaforWrapper running')
		self.__call_semafor()
		self.__parse_semafor()
		print(str(datetime.datetime.now()) + ' SemaforWrapper running finished')
	
	def get_graph(self):
		return self.g
		
# END SemaforWrapper		
