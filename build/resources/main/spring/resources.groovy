import opennlp.ModelService
import opennlp.MongodbService
import opennlp.NlpService
import opennlp.TrainModelService

// Place your Spring DSL code here
beans = {

    mongodbService(MongodbService, ref('grailsApplication'))

    modelService(ModelService, ref('grailsApplication'))

    nlpService(NlpService, ref('grailsApplication'), ref('sanitizeService'), ref('modelService'), ref('mongodbService'))

    trainModelService(TrainModelService, ref('grailsApplication'), ref('mongodbService'))

}
