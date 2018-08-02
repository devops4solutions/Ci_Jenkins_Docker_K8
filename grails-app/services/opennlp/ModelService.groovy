package opennlp

import opennlp.tools.coref.DefaultLinker
import opennlp.tools.coref.Linker
import opennlp.tools.coref.LinkerMode
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.RegexNameFinder
import opennlp.tools.namefind.TokenNameFinder
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.parser.Parser
import opennlp.tools.parser.ParserFactory
import opennlp.tools.parser.ParserModel
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.Tokenizer
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.ObjectStream
import opennlp.tools.util.PlainTextByLineStream

import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * Created by nick on 9/2/15.
 */
class ModelService {

    private def grailsApplication

    // Model Locations
    private final String TOKEN_MODEL_LOC, POS_MODEL_LOC, SENT_FIND_MODEL_LOC,
            PARSER_MODEL_LOC, PERSON_MODEL_LOC, LOCATION_MODEL_LOC,
            DATE_MODEL_LOC, DATE_REGEX, COREF_DIR

    ModelService( def grailsApplication){
        // grailsApplication.config.repos.customModels
        this.grailsApplication = grailsApplication
        TOKEN_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-token.bin"
        POS_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-pos-maxent.bin"
        SENT_FIND_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-sent.bin"
        PARSER_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-parser-chunking.bin"
        PERSON_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-ner-person.bin"
        LOCATION_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-ner-location.bin"
        DATE_MODEL_LOC = grailsApplication.config.repos.opennlpModels + "en-ner-date.bin"
        DATE_REGEX = grailsApplication.config.repos.opennlpModels + "/training/date-regex.train"
        COREF_DIR = grailsApplication.config.repos.opennlpModels + "coref"

    }

    // Use the get method to access these.
    private Tokenizer tokenizer;
    private Parser parser;
    private Linker linker;
    private POSTaggerME tagger;
    private TokenNameFinder regexDateFinder;
    private NameFinderME personNer, locationNer, dateNer;
    // switch to create sentence detector
    private SentenceDetectorME sentenceDetector;
    private def sentenceDetectors = [:]

    // public helpers
    public static String ENTITY_NAME = "personNer"
    public static String ENTITY_LOCATION = "locationNer"
    public static String ENTITY_DATE = "dateNer"

    /**
     *
     * Refactor the code below, it was a quick write to create these beans
     * This will create a tokenizer
     * @return
     */
    def tokenizer() {
        if(tokenizer == null) {
            InputStream modelIn = new FileInputStream(TOKEN_MODEL_LOC)

            try {
                TokenizerModel model = new TokenizerModel(modelIn)
                tokenizer = new TokenizerME(model)
            }
            catch (IOException e) {
                e.printStackTrace()
            }
            finally {
                if (modelIn != null) {
                    try { modelIn.close(); }
                    catch (IOException e) { }
                }
            }

        }
        return tokenizer
    }

    def sentenceDetector(String fileName='en-sent.bin', def custom=false) {

        def sd = sentenceDetectors.get(fileName);

        if(sd == null) {

            String fullPath;
            if(custom) {
                fullPath = grailsApplication.config.repos.customModels + fileName
            } else {
                fullPath = grailsApplication.config.repos.opennlpModels + fileName
            }

            InputStream modelIn = new FileInputStream(fullPath);
            SentenceModel model = null
            try {
                model = new SentenceModel(modelIn);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (modelIn != null) {
                    try { modelIn.close() }
                    catch (IOException e) { }
                }
            }

            sd = new SentenceDetectorME(model)
            sentenceDetectors.put(fileName, sd)
        }
        return sd
    }

    def parser() {
        if(parser == null) {
            InputStream modelIn = new FileInputStream(PARSER_MODEL_LOC)
            try {
                ParserModel model = new ParserModel(modelIn)
                parser = ParserFactory.create(model)
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (modelIn != null) {
                    try { modelIn.close(); }
                    catch (IOException e) { }
                }
            }
        }
        return parser
    }

    def linker() {
        if(linker == null){
            try {
                // LinkerMode needs to be TEST, EVAL does not work.
                linker = new DefaultLinker(COREF_DIR, LinkerMode.TEST);
            } catch (final IOException ioe) {
                ioe.printStackTrace();
            }

        }
        return linker
    }

    def posTagger() {
        if(tagger == null){
            InputStream modelIn = new FileInputStream(POS_MODEL_LOC);
            try {
                POSModel model = new POSModel(modelIn)
                tagger = new POSTaggerME(model)
            } catch (IOException e) {
                // Model loading failed, handle the error
                e.printStackTrace()
            } finally {
                if (modelIn != null) {
                    try {  modelIn.close() }
                    catch (IOException e) { }
                }
            }
        }

        return tagger

    }

    def regexDateFinder() throws Exception {
        if(regexDateFinder == null){
            Charset charset = Charset.forName("UTF-8");
            InputStream regexIs = new FileInputStream(DATE_REGEX)
            ObjectStream<String> lineStream = new PlainTextByLineStream(regexIs, charset);
            String line = null;
            List<Pattern> patterns = new ArrayList<Pattern>();
            while((line = lineStream.read()) != null){
                if(!line.startsWith("#")){
                    patterns.add(Pattern.compile(line));
                }
            }
            Pattern[] array = patterns.toArray(new Pattern[patterns.size()]);
            regexDateFinder = new RegexNameFinder(array, "date");
        }
        return regexDateFinder;
    }

    def entityFinder(String type) {

        if(ENTITY_NAME == type) {
            if(!personNer){
                //personNer = new NameFinderME(new TokenNameFinderModel(this.class.getResourceAsStream(PERSON_MODEL_LOC)))
                personNer = new NameFinderME(new TokenNameFinderModel(new FileInputStream(PERSON_MODEL_LOC)))

            }
            return personNer

        } else if(ENTITY_LOCATION == type) {
            if(!locationNer){
                locationNer = new NameFinderME(new TokenNameFinderModel(new FileInputStream(LOCATION_MODEL_LOC)))
            }
            return locationNer

        } else if (ENTITY_DATE == type) {
            if(!dateNer){
                dateNer = new NameFinderME(new TokenNameFinderModel(new FileInputStream(DATE_MODEL_LOC)))
            }
            return dateNer
        } else {
            return null
        }


    }

}
