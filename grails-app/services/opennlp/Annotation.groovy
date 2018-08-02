package opennlp

import opennlp.tools.util.Span

/**
 * Created by nick on 6/5/15.
 */
class Annotation implements Comparable {
    String name;
    Span span;
    double probability;
    String source;

    public Annotation(String name, Span span, double prob){
        this.name = name;
        this.span = span;
        this.probability = prob;
    }

    public Annotation(String name, Span span, double prob, String source){
        this.name = name;
        this.span = span;
        this.probability = prob;
        this.source = source;
    }

    public int compareTo(Object o) {
        Annotation that = (Annotation)o;

        if(this.span.getStart() < that.getSpan().getStart()){
            return 1;
        } else if(this.span.getEnd() > that.getSpan().getEnd()){
            return 1;
        }
        return 0;
    }
}
