package opennlp

import opennlp.tools.namefind.RegexNameFinder
import opennlp.tools.sentdetect.SentenceDetectorFactory
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.sentdetect.SentenceSample
import opennlp.tools.sentdetect.SentenceSampleStream
import opennlp.tools.util.ObjectStream
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import opennlp.tools.util.model.BaseModel

import java.nio.charset.Charset
import java.util.regex.Pattern

class TrainModelService {
    protected static lang = "en"
    protected static Charset charset = Charset.forName("UTF-8")
    private MongodbService mongodbService
    private def grailsApplication

    public TrainModelService(def grailsApplication, def mongodbService){
        this.mongodbService = mongodbService
        //grailsApplication.config.repos.customModels
        this.grailsApplication = grailsApplication
    }

    def getCurrentModels() {
        def modelListJSON = mongodbService.findModels()
        return modelListJSON
    }

    /**
     * Input must be in the form of pattern per line.
     * @return
     * @throws Exception
     */
    private def createRegExModel(def input ) throws Exception {
        //TokenNameFinder regexModel;
        List<Pattern> patterns = new ArrayList<Pattern>();

        if(input instanceof InputStream) {
            ObjectStream<String> lineStream = new PlainTextByLineStream(input, charset);
            String line = null;

            while((line = lineStream.read()) != null){
                // ignore comments
                if(!line.startsWith("#")){
                    patterns.add(Pattern.compile(line));
                }
            }
        } else if(input instanceof String){
            input.split(System.lineSeparator()).each {
                if(!it.startsWith("#")){
                    patterns.add(Pattern.compile(it));
                }
            }
        }

        Pattern[] array = patterns.toArray(new Pattern[patterns.size()]);
        RegexNameFinder regexModel = new RegexNameFinder(array, "date");

        return regexModel;
    }

    /*
    TODO: Abstract this method to be able to train based on the 'nlpType' selected by the user.
    currently it only trains sentence models.
     */
    public void train(def req) throws Exception {
        // input should be an InputStream, then transform that to a string stream for sentence training.
        // TODO this should be abstracted to handled different types
        ObjectStream<String> lineStream = new PlainTextByLineStream(req['trainingData'], charset);
        ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream);

        // abstract the model to base model for future implementation training examples.
        BaseModel model = null;
        try {
            model = SentenceDetectorME.train(req['language'], sampleStream, true, null, TrainingParameters.defaultParams());
        } finally {
            sampleStream.close();
        }

        // TODO, we should probably save the training data - should we add a new training repo?

        // Create the file and save it to the custom model repo
        OutputStream modelOut = null;
        try {
            def fileName = createFileName(req)
            req.put('fileName',fileName)
            def fullPath = grailsApplication.config.repos.customModels + '/' + fileName
            modelOut = new BufferedOutputStream(new FileOutputStream(fullPath));
            model.serialize(modelOut);
            mongodbService.insertModel(req)
        } finally {
            if (modelOut != null) {
                modelOut.close();
            }
        }

        // TODO run model on test data!

    }

    private def createFileName(def req) {
        def fileName =
                req['language'] + "-" +
                req['modelName'] + "-" +
                req['nlpType'] + "-" +
                req['version'] + ".bin";
        return fileName
    }

}
