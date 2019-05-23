package com.bupsolutions.polaritydetection;

public final class Settings {
    private static final String OPENNLP_DIR = "opennlp/";
    public static final String LANGUAGE_DETECTION_PROFILES = "profiles";

    public static String lang = "en";
    public static final String EN_SENTENCE_DETECTOR = OPENNLP_DIR + "en-sent.bin";
    public static final String EN_TOKENIZER = OPENNLP_DIR + "en-token.bin";
    public static final String EN_POS_TAGGER = OPENNLP_DIR + "en-pos-maxent.bin";
    public static final String EN_LEMMATIZATION_DICTIONARY = OPENNLP_DIR + "en-dictionary.dict";

    public static final String EN_STOPWORDS = "en-stopwords.txt";
    public static final String IT_STOPWORDS = "it-stopwords.txt";

    public static final String IT_SENTENCE_DETECTOR = OPENNLP_DIR + "it-sent.bin";
    public static final String IT_POS_TAGGER = OPENNLP_DIR + "it-pos-maxent.bin";
    public static final String IT_TOKENIZER = OPENNLP_DIR + "it-token.bin";

    private static final String DATA_EN = "../data/en/";

    private static final String DATA_IT = "../data/it/";
    public static final String IMDB_TRAINING_DATA = DATA_EN + "aclImdb/train";

    public static final String IMDB_TEST_DATA = DATA_EN + "aclImdb/test";

    public static final String ROTTEN_IMDB = DATA_EN + "rotten_imdb/";

    public static final String MULTIDOMAIN_DATA = DATA_EN + "Multidomain";
    public static final String SENTIPOLC2016_TRAINING_DATA = DATA_IT + "sentipolc/training/training_set_sentipolc16.csv";

    public static final String SENTIPOLC2016_TEST_DATA = DATA_IT + "sentipolc/test/test_set_sentipolc16.csv";
    private static final String WORD_VECTORS = "../WordVectors/";

    private static final String GLOVE_6B = WORD_VECTORS + "glove.6B/";

    public static final String GLOVE_6B_300 = GLOVE_6B + "glove.6B.300d.txt";

    public static final String GLOVE_ITA = WORD_VECTORS + "glove-ita-wikipedia-300/glove-ita-wikipedia-300.txt";
    public static final String SKIPGRAM_NEGSAMP_ITA = WORD_VECTORS + "skipgram-negsamp-ita-wiki-300/skipgram-negsamp-ita-wiki-300.txt";

    public static final String SENTI_WORD_NET_PATH = "../SentiWordNet_3.0.txt";

}
