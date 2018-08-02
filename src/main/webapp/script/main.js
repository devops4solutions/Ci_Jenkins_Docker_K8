var common = {
	POS_DESC: {
		CC: "Coordinating conjunction",
		CD: "Cardinal number",
		DT: "Determiner",
		EX: "Existential there",
		FW: "Foreign word",
		IN: "Preposition or subordinating conjunction",
		JJ: "Adjective",
		JJR: "Adjective, comparative",
		JJS: "Adjective, superlative",
		LS: "List item marker",
		MD: "Modal",
		NN: "Noun, singular or mass",
		NNS: "Noun, plural",
		NNP: "Proper noun, singular",
		NNPS: "Proper noun, plural",
		PDT: "Predeterminer",
		POS: "Possessive ending",
		PRP: "Personal pronoun",
		'PRP$': "Possessive pronoun",
		RB: "Adverb",
		RBR: "Adverb, comparative",
		RBS: "Adverb, superlative",
		RP: "Particle",
		SYM: "Symbol",
		TO: "to",
		UH: "Interjection",
		VB: "Verb, base form",
		VBD: "Verb, past tense",
		VBG: "Verb, gerund or present participle",
		VBN: "Verb, past participle",
		VBP: "Verb, non-3rd person singular present",
		VBZ: "Verb, 3rd person singular present",
		WDT: "Wh-determiner",
		WP: "Wh-pronoun",
		'WP$': "Possessive wh-pronoun",
		WRB: "Wh-adverb",
		'-LRB-': "Left round bracket",
		'-RRB-': "Right round bracket",
		'-LSB-': "Left square bracket",
		'-RSB-': "Right square bracket",
		'-LCB-': "Left curly bracket",
		'-RCB-': "Right curly bracket",
		ADJP: "Adjective phrase",
		ADVP: "Adverb phrase",
		NP: "Noun phrase",
		PP: "Prepositional phrase",
		S: "Sentence",
		SBAR: "Subordinate Clause",
		SBARQ: "Direct question introduced by wh-element",
		SINV: "Declarative sentence with subject-aux inversion",
		SQ: "Yes/no questions and subconstituent of SBARQ (Direct question introduced by wh-element) excluding wh-element",
		VP: "Verb phrase",
		WHADVP: "Wh-adverb phrase",
		WHNP: "Wh-noun phrase",
		WHPP: "Wh-prepositional phrase",
		X: "Constituent of unknown or uncertain category",
		'*': "Understood subject of infinite or imperative",
		'0': "Zero variant of that in subordinate clauses",
		T: "Trace of wh-Constituent"
	},

	textList: [
        'The word "Scientist" first appeared in 1833.',
        'A 10-Year-Old Accidentally Created in 2012 a New Molecule in Science Class: Tetranitratoxycarbon.',
        '"Sphenopalatine ganglioneuralgia" is the scientific term for brain freeze.',
        'Four Japanese scientists measured the amount of friction between a shoe, a banana skin and the floor: it\'s 0.07.',
        'The World\'s oldest known creature, a mollusc, was 507 years old until scientists killed it by mistake.',
        'Eating salmon helps hair grow faster.',
        'During photosynthesis, plants emit light, called fluorescence, that humans can\'t see.',
        'Sunflowers can be used to clean up radioactive waste.',
        'It would take light 100,000 years to travel from one end of the Milky Way galaxy to the other.',
        'If you could fold a piece of paper 50 times, its thickness would exceed the distance from here to the Sun.'
    ],

    getRandomInt: function() {
    	var min = 0, max = 10;
    	return Math.floor(Math.random() * (max - min)) + min;
    },

    generateText: function(id) {
    	var text = common.textList[common.getRandomInt()];
    	$('#textInput').val(text);
    }

}

var sent = {

	init: function() {
		/* initialize buttons on page */
		$("#generateSentencesButton").button();
		$("#findSentenceButton").button();

		/* events */
		$('#generateSentencesButton').click(sent.generateText);
		$('#findSentenceButton').click(sent.findSentences);
		$('#sentModelDiv').hide();

        /* add select box to select models */
        sent.refreshDropdown();

	},

	refreshDropdown : function() {
	    var $sentSelect = $('#sentModelSelect');
        if(document.getElementById('isentences')) {
            $sentSelect = $(document.getElementById('isentences').contentWindow.document.getElementById('sentModelSelect'));
        }
        $sentSelect.children('option').remove();
        providedModels.findModels(sent.modelSelection, true, 'Sentence Segmentation');
        providedModels.findModels(sent.modelSelection, false, 'Sentence Segmentation');
	},

	modelSelection : function(options) {

	    // because we filter them there is an array of only one.
	    var data = options.data[0];

        if(data && data.models) {

            var $sentSelect = undefined;
            var $sentSelectDiv = undefined;

            // Check where we are - we may be calling this from outside the page (going in throug the iframe)
            if(document.getElementById('isentences')) {
                $sentSelect = $(document.getElementById('isentences').contentWindow.document.getElementById('sentModelSelect'));
                $sentSelectDiv = $(document.getElementById('isentences').contentWindow.document.getElementById('sentModelDiv'));
            } else {
                $sentSelect = $('#sentModelSelect');
                $sentSelectDiv = $('#sentModelDiv');
            }

            for(var i = 0; i < data.models.length; i++) {
                var label =  options.custom ? (data.models[i]) + ' (Custom)' : (data.models[i]) + ' (OpenNLP)';
                $sentSelect.append($('<option></option>',{ value:data.models[i], text:label }));

                // set the default selected value as the NLP model
                if(!options.custom) {
                    $sentSelect.val(data.models[i]);
                }
            }

            var optionsLen = $sentSelect.children('option').length;
            if(optionsLen > 1) {
                $sentSelectDiv.show();
            }

        }

	},

	generateText: function() {
		var text = "";
		for(var i = 0; i < common.textList.length; i++) {
			text = text + common.textList[i] + " ";
		}

		$('#textInput').val(text);
	},

	processResults: function(data) {
		var sentences = data.sentences;
		var $resdiv = $('#sentenceResults');
		$resdiv.empty();
		$(sentences).each(function( index ) {
			$resdiv.append('<div style="width:10px; display:inline-block; padding-right:5px; vertical-align:top;">'+(index+1)+') </div><div class="token" style="width:80%">'+ this +'</div><br>');
		});
	},

	findSentences: function() {

		var postData = {};
		postData.sentModel = $('#sentModelSelect').val();
		postData.sentencesText = $('#textInput').val();

		$.ajax({
			type:'POST',
			data:postData,
			url:'nlp/sentenceFinder',
			success:function(data,textStatus){ sent.processResults(data); },
			error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); }
		 });

	}
}


var token = {

	init: function() {
		/* initialize buttons on page */
		$("#generateTextButton").button();
		$("#tokenizeTextButton").button();

		/* events */
		$('#generateTextButton').click(common.generateText);
		$('#tokenizeTextButton').click(token.tokenizeText);

		var maxEntText = 'In statistics and information theory, a maximum entropy probability distribution is a probability distribution whose entropy is at least as great as that of all other members of a specified class of distributions.';
		$('.maxEntTT').attr('title',maxEntText);
		$('.maxEntTT').tooltip();
	},

	processResults: function(data) {

		var tokens = data.tokens;
		var $resdiv = $('#tokenResults');
		$resdiv.empty();

		$(tokens).each(function( index ) {
			  $resdiv.append('<div class="token">'+ this +'</div>');
		});

	},

	tokenizeText: function() {

		var postData = {};
		postData.tokenizeText = $('#textInput').val();

		$.ajax({
			type:'POST',
			data:postData,
			url:'nlp/tokenize',
			success:function(data,textStatus){ token.processResults(data); },
			error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); }
		 });
	}

}

var pos = {

	init: function() {

		/* initialize buttons on page */
		$("#generateTextButton").button();
		$("#findTagsButton").button();

		/* events */
		$('#generateTextButton').click(common.generateText);
		$('#findTagsButton').click(pos.findTags);

	},

	processResults: function(data) {

		var tags = data.tags;
		var $resdiv = $('#tagResults');
		$resdiv.empty();

		$(tags).each(function( index ) {
			var iprob = (this.prob * 100);
			var tprob = iprob.toFixed(2);
			var titleText = "<b>"+ this.tag + "</b>: " + pos.getPOSDesc(this.tag)
				+ "<br><b>Probability</b>: " + tprob + "%";
			$resdiv.append('<div class="token" style="text-align:center;">(<a class="jqtt" href="javascript:void(0);" title="'+titleText+'">' + this.tag + '</a>)<br>'+this.val+'</div>');
		});

		$('.jqtt').tooltip({
			// This callback renders the value of the HTML tags, rather than the HTML printed to the screen.
			content: function(callback) {
				callback($(this).prop('title').replace('^', '^'));
			}
		});
	},

	findTags: function() {

		var postData = {};
		postData.text = $('#textInput').val();

		$.ajax({
			type:'POST',
			data:postData,
			url:'nlp/partofspeech',
			success:function(data,textStatus){ pos.processResults(data); },
			error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); }
		 });
	},

	getPOSDesc: function(code) {
		var desc = common.POS_DESC[code];
		if(!desc){
			desc = "N/A";
		}
		return desc;
	}

}

var parser = {
	init: function() {
		/* initialize buttons on page */
		$("#generateTextButton").button();
		$("#parserButton").button();

		/* events */
		$('#generateTextButton').click(common.generateText);
		$('#parserButton').click(parser.parseText);
	},

    graphData: function(data, textStatus) {

        var width = 700, height = 400;
        var cluster = d3.layout.cluster().size([height, width - 160]);
        var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });

        var svg = d3.select("#parserResults")
            .append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(40,0)");

        var nodes = cluster.nodes(data),
            links = cluster.links(nodes);

        var link = svg.selectAll(".link")
            .data(links)
            .enter().append("path")
            .attr("class", "link")
            .attr("d", diagonal);

        var node = svg.selectAll(".node")
            .data(nodes)
            .enter().append("g")
            .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })

        node.append("circle").attr("r", 4.5);

        node.append("text")
            .attr("class", "jqtt")
            .attr("title", function(d) {
                    var desc = common.POS_DESC[d.name];
                    if(!desc) {desc = d.name;}
                    var ttText = '<b>'+d.name+':</b> ' +  desc;
                    return ttText;
                }
            )
            .style("cursor","pointer")
            .attr("dx", function(d) { return d.children ? -8 : 8; })
            .attr("dy", 3)
            .style("text-anchor", function(d) { return d.children ? "end" : "start"; })
            .text(function(d) { return d.name; });

        d3.select(self.frameElement).style("height", height + "px");
                $('.jqtt').tooltip({
                    // This callback renders the value of the HTML tags, rather than the HTML printed to the screen.
                    content: function(callback) {
                        callback($(this).attr('title').replace('^', '^'));
                    }
                });
    },

    parseText: function() {
        $('#parserResults').empty();
        $('#parserResults').append('<div id="progressbar"></div>');

        var progressbar = $( "#progressbar" );
        progressbar.progressbar({ value: false });

        var postData = {}
        postData.text = $('#textInput').val();

        $.ajax({
            type:'POST',
            data:postData,
            url:'nlp/graph',
            success:function(data,textStatus){ parser.graphData(data.children[0],textStatus); },
            error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); },
            complete: function() {$( "#progressbar" ).hide();}
         });

    }

}

var entityFinder = {

	init: function() {
		/* initialize buttons on page */
		$("#sampleTextButton").button();
		$("#findEntitiesButton").button();

		/* events */
		$('#sampleTextButton').click(entityFinder.getSampleText);
		$('#findEntitiesButton').click(entityFinder.findEntities);
	},

	sampleText: 'Former first lady Nancy Reagan was taken to a suburban Los Angeles hospital "as a precaution" Sunday after a fall at her home, an aide said. The 86-year-old Reagan will remain overnight for observation at a hospital in Santa Monica, California, said Joanne Drake, chief of staff for the Reagan Foundation.',

	getSampleText: function() {
		$('#textInput').val(entityFinder.sampleText);
	},

	processResults: function(data) {

		$origDiv = $('#original');
		$origDiv.append('<b><u>Original text with flagged entities:</u></b><br>')

		for(var i = 0; i < data.results.length; i++) {
			var res = data.results[i]
			var annotations = res.annotations;

			var flagged = {}

			for(var ii = 0; ii < annotations.length; ii++) {
				var span = annotations[ii].span;
				var startTkn = span.start
				var endTkn = span.end
				for(var iii = startTkn; iii < endTkn; iii++){

					var iprob = (annotations[ii].probability * 100);
					var tprob = iprob.toFixed(2) + '%';
					var spanInfo = {
						type: annotations[ii].name,
						prob: tprob
					};
					flagged[('n'+iii)] = spanInfo;
				}
			}

			var origTokens = res.originalTokens;
			for(var j = 0; j < origTokens.length; j++) {
				var spanInfo = flagged['n'+j];

				var html = "";
				if(spanInfo){
					var ttText = '<b>Type: </b>' + spanInfo.type + '<br><b>Probability: </b>' + spanInfo.prob;
					html = '<span class="flaggedWTT" title="'+ttText+'">'+origTokens[j]+'</span> ';
				} else {
					html = origTokens[j] + ' '
				}
				$origDiv.append(html);
			}

			$origDiv.append("<br>");
		}

		$('.flaggedWTT').tooltip({
			// This callback renders the value of the HTML tags, rather than the HTML printed to the screen.
			content: function(callback) {
				callback($(this).attr('title').replace('^', '^'));
			}
		});

		$('.flaggedWTT').animate({ backgroundColor: "#FFFF99" }, 500, function(){  $( "#progressbar" ).hide(); });

	},

	findEntities: function() {
		$resDiv = $('#results');
		$resDiv.empty();

		$resDiv.append('<div id="progressbar"></div>');
		var progressbar = $( "#progressbar" );
		progressbar.progressbar({ value: false });

		$resDiv.append('<div id="original"></div>');
		$resDiv.append('<div id="redacted"></div>');
		var postData = {}

		postData.text = $('#textInput').val();

		$.ajax({
			type:'POST',
			data:postData,
			url:'nlp/entityFinder',
			success:function(data,textStatus){ entityFinder.processResults(data); },
			error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); },
			complete: function() {}
		 });
	}
}

var coref = {

    /* This should probably be loaded from a database */
    sampleText: 'Former first lady Nancy Reagan was taken to a suburban Los Angeles hospital "as a precaution" Sunday after a fall at her home, Ronald Reagan said. He said with confidence that she will be fine.',

    colors: ['#99FFFF', '#FFFF99', '#FF99CC', '#99FF99'],

    getSampleText: function() {
        $('#textInput').val(coref.sampleText);
    },

    /* This method should contain anything that should be run when the page loads */
    init: function() {
        /* initialize buttons on page */
        $("#sampleTextButton").button();
        $("#findCorefsButton").button();
        /* events */
        $('#sampleTextButton').click(coref.getSampleText);
        $('#findCorefsButton').click(coref.findCorefs);
    },

    processResults: function(data) {
        $origDiv = $('#original');
        var sentences = data.corefs.sentences;
        var mentionList = data.corefs.mentions;

        var result = '';
        var iStartTmp = '<span style="background-color:$color">';
        var iEnd = '</span>'
        var tracker = {};

        for(var mil = 0; mil < mentionList.length; mil++) {
            var mentions = mentionList[mil];
            for(mi = 0; mi < mentions.length; mi++) {
                var mention = mentions[mi];
                var mSent = sentences[mention.sentence];

                // create a unique key to track changes needed for each sentence.
                var sentKey = 'SENT-' + mention.sentence;
                if(!tracker[sentKey]){
                    tracker[sentKey] = {posChange:0, minStart:mention.startpos, minEnd:mention.endpos};
                }

                // use the template to color code the mentions
                var iStart = iStartTmp.replace('\$color', coref.colors[mil])

                // variables use for inserting style around mentions.
                var cur = tracker[sentKey];
                var posChange = cur.posChange;
                var startPos = +mention.startpos;
                var endPos = +mention.endpos;

                // Adjust starting position based on previous additions to the text
                if(startPos <= cur.minStart) {
                    cur.minStart = mention.startpos;
                } else {
                    startPos = +mention.startpos + posChange ;
                }

                // Adjust the ending position based on previous additions to the text
                // Right now I do not have any logic for overlapping text
                if(endPos <= cur.minEnd) {
                    cur.minEnd = endPos;
                    endPos += iStart.length;
                } else {
                    endPos += posChange + iStart.length;
                }

                var result = [mSent.slice(0, startPos), iStart, mSent.slice(startPos)].join('');
                result = [result.slice(0, endPos), iEnd, result.slice(endPos)].join('');
                sentences[mention.sentence] = result;
                cur.posChange = posChange + iStart.length + iEnd.length;
            }

        }

        $origDiv.append('<b><u>Original text with coreference entities:</u></b><br>')
        for(var si = 0; si < sentences.length; si++) {
            $origDiv.append(sentences[si]+' ');
        }

        $( "#progressbar" ).hide();

    },

    findCorefs: function() {
        $resDiv = $('#results');
        $resDiv.empty();

        $resDiv.append('<div id="progressbar"></div>');
        var progressbar = $( "#progressbar" );
        progressbar.progressbar({ value: false });

        $resDiv.append('<div id="original"></div>');
        var postData = {}

        postData.text = $('#textInput').val();

        $.ajax({
            type:'POST',
            data:postData,
            url:'nlp/coref',
            success:function(data,textStatus){ coref.processResults(data); },
            error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); },
            complete: function() {}
         });
    }
}

var useCase = {

	init: function() {
		/* initialize buttons on page */
		$("#sampleTextButton").button();
		$("#redactButton").button();

		/* events */
		$('#sampleTextButton').click(useCase.getSampleText);
		$('#redactButton').click(useCase.findEntities);
	},

	sampleText: 'Former first lady Nancy Reagan was taken to a suburban Los Angeles hospital "as a precaution" Sunday after a fall at her home, an aide said. The 86-year-old Reagan will remain overnight for observation at a hospital in Santa Monica, California, said Joanne Drake, chief of staff for the Reagan Foundation.',

 	getSampleText: function() {
		$('#textInput').val(useCase.sampleText);
	},

	processResults: function(data) {

		$origDiv = $('#original');
		$origDiv.append('<u><b>Personal Information Highlighted:</b></u><br>')
		$redDiv = $('#redacted');
		$redDiv.append('<br><br><u><b>Personal Information Removed:</b></u><br>')
		$fictDiv = $('#fictional');
		$fictDiv.append('<br><br><u><b>Personal Information Replaced:</b></u><br>')

		for(var i = 0; i < data.results.length; i++) {
			var res = data.results[i]
			var annotations = res.annotations;

			var flagged = {}

			for(var ii = 0; ii < annotations.length; ii++) {
				var span = annotations[ii].span;
				var startTkn = span.start
				var endTkn = span.end
				for(var iii = startTkn; iii < endTkn; iii++){

					var iprob = (annotations[ii].probability * 100);
					var tprob = iprob.toFixed(2) + '%';
					if(annotations[ii].source) { tprob = annotations[ii].source; }
					var spanInfo = {
						type: annotations[ii].name,
						prob: tprob
					};
					flagged[('n'+iii)] = spanInfo;
				}
			}

			var origTokens = res.originalTokens;
			for(var j = 0; j < origTokens.length; j++) {
				var spanInfo = flagged['n'+j];

				var html = "";
				if(spanInfo){
					var ttText = '<b>Type: </b>' + spanInfo.type + '<br><b>Probability: </b>' + spanInfo.prob;
					html = '<span class="flaggedWTT" title="'+ttText+'">'+origTokens[j]+'</span> ';
				} else {
					html = origTokens[j] + ' '
				}
				$origDiv.append(html);
			}

			$origDiv.append("<br>");

			var redSent = res.redactedSentence.replace(/\[PERSON\]/g, '<span style="background-color:#99FF99;">[PERSON]</span>');
			redSent = redSent.replace(/\[LOCATION\]/g, '<span style="background-color:#99FFFF;">[LOCATION]</span>');
			redSent = redSent.replace(/\[DATE\]/g, '<span style="background-color:#FFFF99;">[DATE]</span>');
			$redDiv.append(redSent + "<br>");

			var fictSent = res.replacedSentence.replace(/\[PERSON\]/g, '<span style="background-color:#99FF99;">');
			fictSent = fictSent.replace(/\[\/PERSON\]/g, '</span>');
			fictSent = fictSent.replace(/\[LOCATION\]/g, '<span style="background-color:#99FFFF;">');
			fictSent = fictSent.replace(/\[\/LOCATION\]/g, '</span>');
			fictSent = fictSent.replace(/\[DATE\]/g, '<span style="background-color:#FFFF99;">');
			fictSent = fictSent.replace(/\[\/DATE\]/g, '</span>');
			$fictDiv.append(fictSent + "<br>");
		}

		$('.flaggedWTT').tooltip({
			// This callback renders the value of the HTML tags, rather than the HTML printed to the screen.
			content: function(callback) {
				callback($(this).attr('title').replace('^', '^'));
			}
		});

		$( "#progressbar" ).hide();
		$('.flaggedWTT').animate({ backgroundColor: "#FF99CC" }, 500);
	},

	findEntities: function() {
		$resDiv = $('#results');
		$resDiv.empty();

		$resDiv.append('<div id="progressbar"></div>');
		var progressbar = $( "#progressbar" );
		progressbar.progressbar({ value: false });

		$resDiv.append('<div id="original"></div>');
		$resDiv.append('<div id="redacted"></div>');
		$resDiv.append('<div id="fictional"></div>');
		var postData = {}

		postData.text = $('#textInput').val();

		$.ajax({
			type:'POST',
			data:postData,
			url:'nlp/entityFinder',
			success:function(data,textStatus){ useCase.processResults(data); },
			error:function(XMLHttpRequest,textStatus,errorThrown){ alert(errorThrown); },
			complete: function() {}
		 });
	}
}

var providedModels = {

	init: function() {
	    // load the data from the database.
		providedModels.findModels(providedModels.refreshView, false);
		providedModels.findModels(providedModels.refreshView, true);
	},

	refreshView: function(options) {
	    var $div = $('#'+options.divId);
	    $div.empty();
	    for(var i = 0; i < options.data.length; i++){
            $div.append('<span class="label">'+options.data[i]._id+'</span><br>')
            for(var j = 0; j < options.data[i].models.length; j++){
                $div.append('<span class="subLabel">'+options.data[i].models[j]+'</span><br>')
            }
	    }
	},

	findModels: function(callback, custom, nlpTypeFilter) {
	    // defaults
	    var postData = {};
	    var url = 'trainNlp/getOpenNlpModels';
	    var divId = 'pmContent';

	    if(nlpTypeFilter) {
	        postData.nlpTypeFilter = nlpTypeFilter;
	    }

	    if(custom){
	        url = 'trainNlp/getCustomModels'
	        divId = 'ctmContent';
	    }

		$.ajax({
			type:'POST',
			data:postData,
			url:url,
			success:function(data, textStatus){
			    // execute the callback function and pass in the results and some other info.
			    var options = {data: data, divId: divId, custom: custom};
			    callback(options);
			},
			error:function(XMLHttpRequest,textStatus,errorThrown){ console.log(errorThrown); }
		 });
	}
}


var cube = {
    $htmlCube: undefined,
    orientation:'FRONT',
    FRONT:'FRONT',
    LEFT:'LEFT',
    RIGHT:'RIGHT',
    FRONT:'FRONT',
    BACK:'BACK',
    TOP:'TOP',
    BOTTOM:'BOTTOM',

    init : function(cubeId) {
        cube.$htmlCube = $('#'+cubeId);
        /* enable navigation buttons */
        $('.navCubeFront').on('click', function(){ cube.changeView(cube.FRONT); });
        $('.navCubeLeft').on('click', function(){ cube.changeView(cube.LEFT); });
        $('.navCubeRight').on('click', function(){ cube.changeView(cube.RIGHT); });
        $('.navCubeBack').on('click', function(){ cube.changeView(cube.BACK); });
        $('.navCubeTop').on('click', function(){ cube.changeView(cube.TOP); });
        $('.navCubeBottom').on('click', function(){ cube.changeView(cube.BOTTOM); });
    },

    createCssProps : function(cssPropVal) {
        // all the possibilities of properties cover as many browsers as possible
        var cssBrowserProps = {
            'transform':cssPropVal,
            'WebkitTransform':cssPropVal,
            'MozTransform':cssPropVal,
            'OTransform':cssPropVal,
            'msTransform':cssPropVal
        };
        return cssBrowserProps;
    },

    changeView : function(view) {
        var x = 0, y = 0;
        if(view === cube.orientation) {
            return;
        } else if(view === cube.LEFT) {
            x = 0, y = 90;
        } else if(view === cube.RIGHT) {
            x = 0, y = -90;
        } else if(view === cube.FRONT) {
            x = 0, y = 0;
        } else if(view === cube.BACK) {
            x = 0, y = 180;
        } else if(view === cube.TOP) {
            x = -90, y = 0;
        } else if(view === cube.BOTTOM) {
            x = 90, y = -180;
        }

        // update the cube's css
        cube.$htmlCube.css(cube.createCssProps("rotateX("+x+"deg) rotateY("+y+"deg)"));

        // track the orientation of the cube
        cube.orientation = view;
    }
}

var training = {

    // FYI this is currently being populated asynchronously, so it may not be available right away.
    dropdowns : {
        nlptypes : []
    },

    init : function() {
        // this will populate the select list options
        training.getNlpTypes();
        // create buttons and listeners
        $("#trainModelButton").button();
        $('#trainModelButton').on('click', training.trainModel)
    },

    getNlpTypes : function() {
        $.ajax({
            type:'POST',
            data:{},
            url:'trainNlp/getNlpTypeList',
            success:function(data,textStatus){ training.processResults(data); },
            error:function(XMLHttpRequest,textStatus,errorThrown){ console.log(errorThrown); }
         });
    },

    processResults : function(data) {
        // load the data
        training.dropdowns.nlptypes = data;

        // load the html select
        $('.modelTrainTypeSelect').each(function() {
            for(var i = 0; i < data.length; i++){
                $(this).append($('<option></option>',{ value:data[i].value, text:data[i].label }));
            }
        });

    },

    trainModel : function() {
        $.ajax({
            type:'POST',
            data: training.getFormData(),
            cache: false,
            contentType: false,
            processData: false,
            url:'trainNlp/trainModel',
            success:function(data,textStatus){
                training.processResults(data);
                // update the model views
                providedModels.findModels(providedModels.refreshView, true);
                // update the model list on the execution page
                sent.refreshDropdown();
                training.resetForm(data);
            },
            error:function(XMLHttpRequest,textStatus,errorThrown){ console.log(errorThrown); }
        })
    },

    resetForm : function(data) {
        $('#nlpType').val('');
        $('#tmModelName').val('');
        $('#tdiFile')[0].files[0] = undefined;
        $('#tdiFile').val('');
        $('#regEx').prop('checked', false);

        console.log(JSON.stringify(data));
        if(data.result == 'success') {
            $('#trainingSuccess').empty().append('Model Successfully Added');
            $('#trainingSuccess').show();
            $('#trainingSuccess').effect('drop', {duration:5000, direction:'down'});
        }
    },

    getFormData : function() {
        var formData = new FormData();
        formData.append('trainingDataFile', $('#tdiFile')[0].files[0]);
        formData.append('modelName', $('#tmModelName').val());
        // TODO: Select and Checkbox
        var regex = $('#regEx').prop('checked') ? true : false;
        formData.append('isRegEx', regex);
        formData.append('nlpType', $('#nlpType').val());
        return formData;
    }

}

var nlpApp = {
    init : function() {
        /* create tabs */
        $("#MenuTabs").tabs({active:0});
        $("#ExampleTabs").tabs({active:0});
        /* init training page */
        training.init();
        providedModels.init();
        cube.init('cube');
    }
}
