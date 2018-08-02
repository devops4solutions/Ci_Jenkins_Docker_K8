package opennlp

import com.mongodb.MongoClient
import com.mongodb.client.AggregateIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import static java.util.Arrays.asList;

class MongodbService {

    static String NLP_SENT = "Sentence"
    static String NLP_TOKEN = "Tokenization"
    static String NLP_POS = "POS"
    static String NLP_PARSE = "Parser"
    static String NLP_NER = "NER"
    static String NLP_USE_CASE = "UseCase"
    static String NLP_COREF = "CoRef"

    private MongoClient mongoClient
    private MongoDatabase db
    private MongoCollection modelColl
    private MongoCollection typeColl

    MongodbService(def grailsApplication){
        mongoClient = new MongoClient( grailsApplication.config.mongodb.url )
        db = mongoClient.getDatabase( grailsApplication.config.mongodb.db )
        modelColl = db.getCollection( grailsApplication.config.mongodb.modelColl )
        typeColl = db.getCollection( grailsApplication.config.mongodb.typeColl )
    }

    def findNlpTypes() {
        // use aggregation as a sudo-hack to return only the values I want. I don't care about the ID
        //db.nlp_types.aggregate({$project:{_id:0, label:1, value:1}})
        def types = typeColl.aggregate(asList(
            new Document('$project', new Document('_id', 0).append('label', 1).append('value', 1))
        ))
        return types
    }

    def findModelByName(String modelName) {
        Document matchDoc = new Document('displayName',modelName)
        modelColl.find(matchDoc).first()
    }

    def findModels(def modelType = null) {
        println "using filter $modelType"
        // this will get a list of model names that are not test and not custom (the OpenNLP models).
        //db.nlp_models.aggregate({$match:{isCustom:false, isTest:false}}, { $group:{ _id:'$nlpProcess', models:{$push:'$displayName'} } }, {$sort:{_id:1}})

        Document matchDoc =  new Document('isCustom', Boolean.FALSE).append('isTest', Boolean.FALSE)
        if(modelType) {
            matchDoc.append('nlpProcess', modelType)
        }

        def models = modelColl.aggregate(asList(
            new Document('$match', matchDoc),
            new Document('$group', new Document('_id', '$nlpProcess').append('models', new Document('$push', '$displayName'))),
            new Document('$sort', new Document('_id', new Integer(1)))
        ));
        return models
    }

    def findCustomModels(def modelType = null) {
        println "using filter $modelType"
        // this will get a list of model names that are not test and not custom (the OpenNLP models).
        //db.nlp_models.aggregate({$match:{isCustom:false, isTest:false}}, { $group:{ _id:'$nlpProcess', models:{$push:'$displayName'} } }, {$sort:{_id:1}})

        Document matchDoc =  new Document('isCustom', Boolean.TRUE)
        if(modelType) {
            matchDoc.append('nlpProcess', modelType)
        }

        def models = modelColl.aggregate(asList(
                new Document('$match', matchDoc),
                new Document('$group', new Document('_id', '$nlpProcess').append('models', new Document('$push', '$displayName'))),
                new Document('$sort', new Document('_id', new Integer(1)))
        ));
        return models
    }

    def insertModel(def req) {
        /*
        Mappings mongodb schema to req:
        "displayName"   : 'modelName'
        "resourceName"  : 'fileName'
        "nlpProcess"    : 'nlpType'
        "modelVersion"  : 'version'
        "isRegEx"       : 'regEx'
        "isTest"        : 'test'
        "isCustom"      : 'custom'
        "language"      : 'language' hardcoded for now
        */

        // for now only match on file name and version.
        def filterDoc = new Document('resourceName', req['fileName'])
                .append('modelVersion', req['version'])

        def updateDoc = new Document('displayName', req['modelName'])
                .append('resourceName', req['fileName'])
                .append('nlpProcess', req['nlpType'])
                .append('modelVersion', req['version'])
                .append('isRegEx', req['regEx'])
                .append('isTest', req['test'])
                .append('isCustom', req['custom'])
                .append('language', new Document('name','English').append('code','en'))
        // upsert the entire document... this will update existing records and also add new ones.
        UpdateResult res = modelColl.replaceOne(filterDoc, updateDoc, new UpdateOptions().upsert(true))

        return [matched:res.matchedCount, updated:res.modifiedCount]

    }

}
