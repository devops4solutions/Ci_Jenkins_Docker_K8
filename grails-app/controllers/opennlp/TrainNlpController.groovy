package opennlp

import grails.converters.JSON

class TrainNlpController {

    MongodbService mongodbService
    TrainModelService trainModelService

    def getOpenNlpModels() {
        def models = mongodbService.findModels(params.nlpTypeFilter).asList()
        render models as JSON
    }

    def getCustomModels() {
        def cust = mongodbService.findCustomModels(params.nlpTypeFilter).asList()
        render cust as JSON
    }

    def getNlpTypeList() {
        def types = mongodbService.findNlpTypes().asList()
        render types as JSON
    }

    def trainModel() {
        // set the defaults for now - these should be input TODO: add later
        def req = [
                'language':'en',
                'version':'1.0',
                'test':Boolean.valueOf('true'),
                'custom':Boolean.valueOf('true')
        ]

        println('getting file...')
        if (params.trainingDataFile) {
            println 'we have a file!'
            def trainingDataFile = request.getFile('trainingDataFile')
            req.put('trainingData', trainingDataFile.getInputStream())
        } else  {
            println 'Houston, we do not have a file.'
            // This is a problem...
        }

        req.put('modelName', params.modelName)
        req.put('regEx', Boolean.valueOf(params.isRegEx))
        req.put('nlpType', params.nlpType)

        trainModelService.train(req);
        // run the model with some test data...

        def ret = ['result':'success']
        render ret as JSON

    }

}
