package opennlp

class SanitizeService {

    static final String PERSON_TYPE = 'person';
    static final String LOCATION_TYPE = 'location';
    static final String DATE_TYPE = 'date';

    def fictionalNames = [
        'Daffy Duck',
        'Elmer Fudd',
        'Wile E Coyote',
        'Yosemite Sam',
        'Daisy Duck',
        'Bugs Bunny',
        'Tweety',
        'Pepe Le Pew',
        'Sylvester',
        'Tasmanian Devil'
    ];

    def fictionalLocations = [
        'Aquaria',
        'Gallifrey',
        'Caprica',
        'Icarus',
        'Picon',
        'Gemenon',
        'Tauron',
        'Leonis',
        'Virgon',
        'Libran'
    ];

    def monthDays = [
        0,  // empty - index 1 based
        31, // jan
        28, // feb
        31, // mar
        30, // apr
        31, // may
        30, // jun
        31, // jul
        31, // aug
        30, // sept
        31, // oct
        30, // nov
        31  // dec
    ];

    def getRandomDate(){
        def randomMonth = getRandomInt(1,12)
        def randomDay = getRandomInt(1, monthDays[randomMonth])
        def randomYear = getRandomInt(1970, 2020)
        (randomMonth + '/' + randomDay + '/' + randomYear)
    }

    def getRandomName() {
        fictionalNames[getRandomInt(0,fictionalNames.size())];
    }

    def getRandomLocation() {
        fictionalLocations[getRandomInt(0,fictionalLocations.size())];
    }

    Integer getRandomInt(Integer min, Integer max) {
        if(!min){ min = 0; }
        if(!max) { max = 10; }
        Math.floor(Math.random() * (max - min)) + min;
    }

    def getReplacementVal(String inputType) {

        def ret = null;

        if(PERSON_TYPE.equalsIgnoreCase(inputType)){
            ret = getRandomName()
        } else if(LOCATION_TYPE.equalsIgnoreCase(inputType)){
            ret = getRandomLocation()
        } else if(DATE_TYPE.equalsIgnoreCase(inputType)){
            ret = getRandomDate()
        }
        ret
    }
}
