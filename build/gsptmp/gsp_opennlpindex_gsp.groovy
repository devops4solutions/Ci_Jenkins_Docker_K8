import grails.plugins.metadata.GrailsPlugin
import org.grails.gsp.compiler.transform.LineNumber
import org.grails.gsp.GroovyPage
import org.grails.web.taglib.*
import org.grails.taglib.GrailsTagException
import org.springframework.web.util.*
import grails.util.GrailsUtil

class gsp_opennlpindex_gsp extends GroovyPage {
public String getGroovyPageFileName() { "/WEB-INF/grails-app/views/index.gsp" }
public Object run() {
Writer out = getOut()
Writer expressionOut = getExpressionOut()
registerSitemeshPreprocessMode()
printHtmlPart(0)
createTagBody(1, {->
printHtmlPart(1)
invokeTag('captureMeta','sitemesh',9,['gsp_sm_xmlClosingForEmptyTag':("/"),'name':("layout"),'content':("main")],-1)
printHtmlPart(2)
createTagBody(2, {->
createClosureForHtmlPart(3, 3)
invokeTag('captureTitle','sitemesh',10,[:],3)
})
invokeTag('wrapTitleTag','sitemesh',10,[:],2)
printHtmlPart(4)
})
invokeTag('captureHead','sitemesh',83,[:],1)
printHtmlPart(5)
createTagBody(1, {->
printHtmlPart(6)
invokeTag('message','g',85,['code':("default.link.skip.label"),'default':("Skip to content&hellip;")],-1)
printHtmlPart(7)
expressionOut.print(grails.util.Environment.current.name)
printHtmlPart(8)
expressionOut.print(grailsApplication.config.grails?.profile)
printHtmlPart(9)
invokeTag('meta','g',91,['name':("info.app.version")],-1)
printHtmlPart(10)
invokeTag('meta','g',92,['name':("info.app.grailsVersion")],-1)
printHtmlPart(11)
expressionOut.print(GroovySystem.getVersion())
printHtmlPart(12)
expressionOut.print(System.getProperty('java.version'))
printHtmlPart(13)
expressionOut.print(grails.util.Environment.reloadingAgentEnabled)
printHtmlPart(14)
expressionOut.print(grailsApplication.controllerClasses.size())
printHtmlPart(15)
expressionOut.print(grailsApplication.domainClasses.size())
printHtmlPart(16)
expressionOut.print(grailsApplication.serviceClasses.size())
printHtmlPart(17)
expressionOut.print(grailsApplication.tagLibClasses.size())
printHtmlPart(18)
for( plugin in (applicationContext.getBean('pluginManager').allPlugins) ) {
printHtmlPart(19)
expressionOut.print(plugin.name)
printHtmlPart(20)
expressionOut.print(plugin.version)
printHtmlPart(21)
}
printHtmlPart(22)
for( c in (grailsApplication.controllerClasses.sort { it.fullName }) ) {
printHtmlPart(23)
createTagBody(3, {->
expressionOut.print(c.fullName)
})
invokeTag('link','g',122,['controller':(c.logicalPropertyName)],3)
printHtmlPart(24)
}
printHtmlPart(25)
})
invokeTag('captureBody','sitemesh',127,[:],1)
printHtmlPart(26)
}
public static final Map JSP_TAGS = new HashMap()
protected void init() {
	this.jspTags = JSP_TAGS
}
public static final String CONTENT_TYPE = 'text/html;charset=UTF-8'
public static final long LAST_MODIFIED = 1531878338000L
public static final String EXPRESSION_CODEC = 'html'
public static final String STATIC_CODEC = 'none'
public static final String OUT_CODEC = 'none'
public static final String TAGLIB_CODEC = 'none'
}
