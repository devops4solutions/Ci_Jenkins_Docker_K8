package opennlp

import grails.converters.JSON

class NlpController {
    NlpService nlpService

    def graph() {
        String input = params.text
        def map = nlpService.parser(input);
        render map as JSON
    }

    def sentenceFinder() {
        String input = params.sentencesText
        String model = params.sentModel
        String[] sentences = nlpService.sentenceFinder(input, model)
        def map = [:]
        map.put('sentences', sentences)
        render map as JSON
    }

    def tokenize() {
        String input = params.tokenizeText
        String[] tokens = nlpService.tokenize(input);
        def map = [:]
        map.put('tokens', tokens)
        render map as JSON
    }

    def partofspeech() {
        String input = params.text
        def results = nlpService.partofspeech(input)
        def map = [:]
        map.put('tags', results)
        render map as JSON
    }

    def entityFinder() {
        String input = params.text
        def result = nlpService.entityFinder(input)
        def map = [:]
        map.put("results", result)
        render map as JSON
    }

    def coref() {
        String input = params.text
        def corefs = nlpService.coRef(input)
        def map = [:]
        map.put('corefs', corefs)
        def retJSON = map as JSON
        println retJSON
        render retJSON
    }

}