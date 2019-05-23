from general import *	
from stanfordcorenlp import StanfordCoreNLP
from manager import *
	
class CoreNLPWrapper:
		
	def __init__(self,text,g):
		self.text = text
		self.g = g
		self.corenlp_data = None
		self.corefs = None
		self.sentences = []				
		
		# java -mx4g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000
		self.nlp = StanfordCoreNLP('http://localhost', port=9000)
		#self.nlp = StanfordCoreNLP('http://corenlp.run', port=80)
		
		print(str(datetime.datetime.now()) + ' CoreNLPWrapper built')
		
	def __get_corenlp_out(self):
		self.corenlp_data = {}
		props = {'annotators': 'ssplit,depparse,ner,coref', 'pipelineLanguage': 'en', 'outputFormat': 'json'}
		try:
			corenlp_out = json.loads(self.nlp.annotate(self.text, properties=props))
		except:
			print('Stanford Core NLP is not responding. Please try later')
			exit(1)

		#print(corenlp_out['sentences'][0])
		s_count = 0
		for s in corenlp_out['sentences']:
			self.corenlp_data[s_count] = {}
			text = ''

			for token in s['tokens']:
				text = text + token['originalText'] + token['after']

			self.corenlp_data[s_count] = {'text': text}
			self.corenlp_data[s_count]['tokens'] = s['tokens']
			self.corenlp_data[s_count]['dependencies'] = s['enhancedPlusPlusDependencies']
			s_count += 1
		
		self.corefs = corenlp_out['corefs']
		

	def __corenlp_dependency_parser(self):
		for sentence in self.corenlp_data:
			tokens = self.corenlp_data[sentence]['tokens']
			dependencies = self.corenlp_data[sentence]['dependencies']

			for dependency in dependencies:
				dependency_type = dependency['dep']

				if dependency_type not in ['ROOT', 'punct']:
					n_token_governor = dependency['governor']
					n_token_dependent = dependency['dependent']

					governor = XPROJECT_OFFSET + str(tokens[n_token_governor - 1]['characterOffsetBegin']) + \
						'_' + str(tokens[n_token_governor - 1]['characterOffsetEnd']) + \
						'_' + tokens[n_token_governor - 1]['originalText']

					dependent = XPROJECT_OFFSET + str(tokens[n_token_dependent - 1]['characterOffsetBegin']) + \
						'_' + str(tokens[n_token_dependent - 1]['characterOffsetEnd']) + \
						'_' + tokens[n_token_dependent - 1]['originalText']

					self.g.add((rdflib.term.URIRef(governor), \
						   rdflib.term.URIRef(HAS_DEPENDENCY + dependency_type), \
						   rdflib.term.URIRef(dependent)
					))
					
					self.g.add((rdflib.term.URIRef(governor), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
					))
					
	def __corenlp_ner_pos_parser(self):
		for sentence in self.corenlp_data:
			tokens = self.corenlp_data[sentence]['tokens']

			for token in tokens:
				token_label = token['originalText']
				token_begin = token['characterOffsetBegin']
				token_end = token['characterOffsetEnd']
				token_ner = token['ner']
				token_pos = token['pos']

				if token_pos not in string.punctuation:
					ref = XPROJECT_OFFSET + str(token_begin) + '_' + str(token_end) + '_' + token_label

					self.g.add((rdflib.term.URIRef(ref), \
						   rdflib.term.URIRef(BEGIN), \
						   rdflib.term.Literal(token_begin)
					))

					self.g.add((rdflib.term.URIRef(ref), \
						   rdflib.term.URIRef(END), \
						   rdflib.term.Literal(token_end)
					))

					if token_ner != 'O':
						self.g.add((rdflib.term.URIRef(ref), \
							   rdflib.term.URIRef(HAS_ENTITY_TYPE), \
							   rdflib.term.Literal((token_ner))
						))

					self.g.add((rdflib.term.URIRef(ref), \
						   rdflib.term.URIRef(HAS_POS), \
						   rdflib.term.Literal(token_pos, datatype=XSD.string)
					))
					
					self.g.add((rdflib.term.URIRef(ref), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
					))
						   
	def __corenlp_corefs_parser(self):
		for c in self.corefs:
			start_representative = int(self.corefs[c][0]['startIndex'] - 1)
			end_representative = int(self.corefs[c][0]['endIndex'] - 1)
			sentence_representative = self.corefs[c][0]['sentNum'] - 1
			tokens_representative = self.corenlp_data[sentence_representative]['tokens']

			representative = XPROJECT_OFFSET + str(tokens_representative[start_representative]['characterOffsetBegin']) + '_' + \
							 str(tokens_representative[end_representative - 1]['characterOffsetEnd'])

			i = start_representative
			while( i < end_representative ):
				representative += '_' + tokens_representative[i]['originalText']
				i += 1
			representative = rdflib.URIRef(representative)

			for i in range(1,len(self.corefs[c])):

				if self.corefs[c][i]['type'] == 'PRONOMINAL':
					start = self.corefs[c][i]['startIndex'] - 1
					end = self.corefs[c][i]['endIndex'] - 1
					sentence = self.corefs[c][i]['sentNum'] - 1
					tokens = self.corenlp_data[sentence]['tokens']

					label = XPROJECT_OFFSET + str(tokens[start]['characterOffsetBegin']) + '_' + \
									 str(tokens[end - 1]['characterOffsetEnd'])
					j = start
					while (j < end):
						label += '_' + tokens[j]['originalText']
						j += 1

					match_label = '<http://' + label + '>'

					for (s,p,o) in self.g:
						if s.n3() == match_label:
							self.g.add((s, \
								   rdflib.URIRef(HAS_COREF), \
								   representative
							))
							
							self.g.add((rdflib.term.URIRef(s), \
								OWL_TYPE, \
								XPROJECT_CLASS_WORD
							))							
								   
					for (s,p,o) in self.g:
						if o.n3() == match_label:
							self.g.add((representative, \
								   rdflib.URIRef(IS_COREF), \
								   o
							))
								   
	def __sentences(self):
		for sentence in self.corenlp_data:
			s = ''
			for token in self.corenlp_data[sentence]['tokens']:
				s += token['before'] + token['originalText'] + token['after']
			self.sentences += [s]		
								   
	def run(self):	
		print(str(datetime.datetime.now()) + ' CoreNLPWrapper running')
		self.__get_corenlp_out()
		self.__corenlp_dependency_parser()
		self.__corenlp_ner_pos_parser()
		self.__corenlp_corefs_parser()
		self.__sentences()
		print(str(datetime.datetime.now()) + ' CoreNLPWrapper finished')
	
	def get_graph(self):
		return self.g
	
	def get_corenlp_data(self):
		return self.corenlp_data
		
	def get_sentences(self):
		return self.sentences
	
	def close(self):
		self.nlp.close()
#END CoreNLPWrapper
