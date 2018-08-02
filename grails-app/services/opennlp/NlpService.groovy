package opennlp

import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

class NlpService {

    NlpService(def grailsApplication, def sanitizeService, def modelService, def mongodbService){

        //TODO this should read it from a system variable - it's okay for testing for now.
        if(!System.getProperty("WNSEARCHDIR")){
            println "\nSetting wordnet location: $grailsApplication.config.repos.wordnet \n"
            System.setProperty("WNSEARCHDIR", grailsApplication.config.repos.wordnet)
        }

        this.sanitizeService = sanitizeService
        this.modelService = modelService
        this.mongodbService = mongodbService

    }

    SanitizeService sanitizeService
    ModelService modelService
    MongodbService mongodbService

    def sentenceFinder(String input, String model = null) {

        def fileName = null
        def custom = false

        if(model != null) {
            def doc = mongodbService.findModelByName(model)
            fileName = doc.getString('resourceName')
            custom = doc.getBoolean('isCustom')
        } else {
            // default model
            fileName = 'en-sent.bin'
        }

        println("fileName = $fileName | custom = $custom")

        String[] sentences = modelService.sentenceDetector(fileName, custom).sentDetect(input);
        return sentences
    }

    def tokenize(String text) {
        String[] tokens = modelService.tokenizer().tokenize(text);
        return tokens
    }

    def partofspeech(String input) {
        def results = []
        String[] tokens = tokenize(input)
        String[] tags = modelService.posTagger().tag(tokens);
        double[] probs = modelService.posTagger().probs();

        for (int i = 0; i < tokens.length; i++) {
            def tmpRes = [:]
            tmpRes.put('val',tokens[i])
            tmpRes.put('tag',tags[i])
            tmpRes.put('prob',probs[i])
            results << tmpRes
        }
        return results
    }

    def parser(String input) {
        def tokens = tokenize(input);
        Parse[] topParses = ParserTool.parseLine(tokens.join(" "), modelService.parser(), 1);
        Map map = new HashMap();
        map.put("name", "TOP");
        map.put("children", new ArrayList());
        Map result = createTreeStructure(topParses[0], 1, map);
        return result
    }

    private Map createTreeStructure(Parse p, int level, Map map) {

        for(Parse c : p.getChildren()) {

            if(c.getType() == "TK") {
                Map fMap = new HashMap();
                fMap.put("name", c.toString());
                ((List) map.get("children")).add(fMap);
            } else {
                Map cMap = new HashMap();
                cMap.put("name", c.getType());
                ((List) map.get("children")).add(cMap);

                if(c.getChildCount() > 0) {
                    cMap.put("children", new ArrayList());
                    createTreeStructure(c, (level+1), cMap);
                }
            }
        }

        return map;

    }

    def entityFinder(String input) {
        def ret = []
        def repTokens = [:]

        def nurFinders = [
            modelService.entityFinder(modelService.ENTITY_NAME),
            modelService.entityFinder(modelService.ENTITY_LOCATION),
            modelService.entityFinder(modelService.ENTITY_DATE),
            modelService.regexDateFinder()
        ]
        def nurTypes = ["Person", "Location", "Date", "Date"]
        def sentences = sentenceFinder(input)

        for(String sentence : sentences) {
            def tmpRet = [:]
            List<Annotation> allAnnotations = [];
            String[] tokens = tokenize(sentence)
            int typeIndex = 0;

            for(def nurFinder : nurFinders) {
                Span[] spans = nurFinder.find(tokens);
                double[] probs = null
                if(nurFinder instanceof NameFinderME) {
                    probs = nurFinder.probs(spans);
                }
                for(int i = 0; i < spans.length; i++) {
                    def prob = 0.0
                    def src = "RegEx"
                    if(probs != null) {
                        prob = probs[i]
                        src = null
                    }
                    allAnnotations.add(new Annotation(nurTypes[typeIndex], spans[i], prob, src));
                }
                typeIndex++;
            }

            if(allAnnotations.size() > 0){
                removeConflicts(allAnnotations);
            }

            String redacted = sentence;
            String replaced = sentence;

            for(Annotation ann : allAnnotations) {
                StringBuilder cb = new StringBuilder();
                int count = 0;
                for(int i = ann.span.getStart(); i <  ann.span.getEnd(); i++){
                    if(count > 0){
                        cb.append(" ");
                    }
                    cb.append(tokens[i]);
                    count++;
                }

                String nm = cb.toString();
                def redactedVal = "["+ann.getName().toUpperCase()+"]"
                def redactedValEnd = "[/"+ann.getName().toUpperCase()+"]"
                redacted = redacted.replaceFirst(nm, redactedVal)
                def repVal  = repTokens.get(ann.getName()+"^"+nm)
                if(!repVal){
                    repVal = sanitizeService.getReplacementVal(ann.getName())
                    repTokens.put(ann.getName()+"^"+nm, repVal);
                }

                // or use this to get a random value each time.
                //def repVal  = sanitizeService.getReplacementVal(ann.getName())
                def replaceVal = redactedVal+repVal+redactedValEnd
                replaced = replaced.replaceFirst(nm, replaceVal)
            }

            tmpRet.put("originalSentence", sentence)
            tmpRet.put("originalTokens", tokens)
            tmpRet.put("annotations", allAnnotations)
            tmpRet.put("redactedSentence", redacted)
            tmpRet.put("replacedSentence", replaced)
            ret << tmpRet
        }
        return ret;
    }


    def coRef(String input) {
        def sentences = sentenceFinder(input)
        def mentions = findEntityMentions(sentences)
        def ret = [:]
        ret.put("sentences", sentences)
        ret.put('mentions', [])

        mentions.each {
            if(it.getMentions().size() > 1) {
                def mList = []

                it.getMentions().each { m ->
                    def retobj = [:]
                    retobj.put("key", m.toText())
                    retobj.put("sentence", m.getSentenceNumber())
                    retobj.put("startpos", m.getSpan().getStart())
                    retobj.put("endpos", m.getSpan().getEnd())
                    mList << retobj
                }
                ret.get('mentions') << mList
                //ret.put('mention', mList)

            }

        }
        return ret
    }

    private DiscourseEntity[] findEntityMentions(final String[] sentences) {

        // list of document mentions
        final List<Mention> document = new ArrayList<Mention>();

        for (int i = 0; i < sentences.length; i++) {
            // generate the sentence parse tree
            final Parse parse = parseSentence(sentences[i]);

            def nameNer = modelService.entityFinder(modelService.ENTITY_NAME)
            Span[] personEntites = nameNer.find(modelService.tokenizer().tokenize(sentences[i]));
            Parse.addNames("person", personEntites, parse.getTagNodes());

            final DefaultParse parseWrapper = new DefaultParse(parse, i);
            final Mention[] extents = modelService.linker().getMentionFinder().getMentions(parseWrapper);

            //Note: taken from TreebankParser source...
            for (int ei = 0; ei < extents.length; ei++) {
                // construct new parses for mentions which don't have constituents.
                if (extents[ei].getParse() == null) {
                    // not sure how to get head index, but its not used at this point
                    final Parse snp = new Parse(parse.getText(), extents[ei].getSpan(), "NML", 1.0, 0);
                    parse.insert(snp);
                    println ("Setting new parse for " + extents[ei] + " to " + snp);
                    extents[ei].setParse(new DefaultParse(snp, i));
                }
            }
            document.addAll(Arrays.asList(extents));
        }

        if (!document.isEmpty()) {
            return modelService.linker().getEntities(document.toArray(new Mention[document.size()]));
        }

        return new DiscourseEntity[0];
    }

    /**
     * Convert the provided sentence and corresponding tokens into a parse tree.
     *
     * @param text the sentence text
     * @return the parse tree
     */
    private Parse parseSentence(final String text) {

        final Parse p = new Parse(text, new Span(0, text.length()), AbstractBottomUpParser.INC_NODE, 1, 0);

        final Span[] spans = modelService.tokenizer().tokenizePos(text);

        for (int idx = 0; idx < spans.length; idx++) {
            final Span span = spans[idx];
            // flesh out the parse with token sub-parses
            p.insert(new Parse(text, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
        }

        return modelService.parser().parse(p);
    }

    private static void removeConflicts(List<Annotation> list) {
        Collections.sort(list);
        List<Annotation> stack = new ArrayList<Annotation>();
        stack.add(list.get(0));
        int ai = 0;
        for (Annotation curr : list){
            boolean deleteCurr = false;
            for(int ki = stack.size()-1; ki < 0; ki--){
                Annotation prev = (Annotation) stack.get(ki);
                if(prev.getSpan().equals(curr.getSpan())){
                    if(prev.getProbability() > curr.getProbability()){
                        deleteCurr = true;
                        break;
                    } else {
                        list.remove(stack.remove(ki));
                    }
                } else if(prev.getSpan().intersects(curr.getSpan())) {
                    if(prev.getProbability() > curr.getProbability()) {
                        deleteCurr = true;
                        break;
                    } else {
                        list.remove(stack.remove(ki));
                        ai--;
                    }
                } else if (prev.getSpan().contains(curr.getSpan())) {
                    break;
                } else {
                    stack.remove(ki);
                }
            }

            if(deleteCurr) {
                list.remove(ai);
                ai--;
                deleteCurr = false;
            } else  {
                stack.add(curr);
            }
            ai++;
        }

    }

}
