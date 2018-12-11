@Grapes([
	@Grab( group = "org.codehaus.groovy", module = "groovy-xmlrpc", version = "0.7" ),
	@Grab( group = "org.igniterealtime.smack", module = "smack", version = "3.1.0" )])
@GrabExclude("jivesoftware:smack")
import groovy.net.xmlrpc.XMLRPCServerProxy
import java.util.zip.ZipInputStream

def cli = new CliBuilder(usage: "gsubdl [options] [args]")
cli.with {
	d( longOpt: "destination", argName: "location", "unzip file destination. default: script location", required: false, args: 1 )
	h( longOpt: "help", "usage information", required: false )
	l( longOpt: "language",  "subtitle's languages (separated by comma). default: eng", required: false, args: 1 )
	q( longOpt: "query", argName: "text", "fulltext search by text", required: true, args: 1 )
}
def options = cli.parse(args)

if ( !options ) {
	return
}
if ( options.h ) {
	cli.usage()
	return
}

HTTP_SUCCESS 			= 200
USER_AGENT 				= "gsubdl v1"
SCRIPT_PATH 			= new File( getClass().protectionDomain.codeSource.location.path ).parent
SCRIPT_CONFIG_DIR = new File( SCRIPT_PATH ).parent + File.separator + "conf"

def destination = options.d ?: SCRIPT_PATH
def language 	  = options.l ?: "eng"
def query 		  = options.q

def login = {
	print "Connecting to OpenSubtitles... "
	def loginInfo = server.LogIn( "", "", "eng", USER_AGENT )

	if ( loginInfo.status[0..3].toInteger() != HTTP_SUCCESS ) {
		throw new Exception( "HTTP Response " + loginInfo.status )
	}

	println "done"
	return loginInfo
}

def filterSubtitles = { data ->
	print "Filtering to best result... "

	def subtitles = []
	def languages = []

	if ( language.contains( "," ) ) {
		languages = language.split( "," )
	}
	else {
		languages << language
	}

	languages.each {
		def lang = it

		def subtitle = data.find { (it.MovieReleaseName == query || it.SubFileName - ".srt" ==~ query) && it.SubLanguageID == lang }
		if ( subtitle ) {
			subtitles << subtitle
		}
	}

	if ( subtitles.isEmpty() ) {
		if ( data.isEmpty() ) {
			throw new Exception( "No subtitles were found for the chosen languages" )
		}

		subtitles << data.first()
	}

	println "done"
	return [name: subtitles.first().SubFileName - ".srt", link: subtitles.first().ZipDownloadLink]
}

def findSubtitles = {
	print "Searching for subtitles... "

	def searchInfo = server.SearchSubtitles( token, [[sublanguageid: language, query: query]] )
	def data = searchInfo.data

	if ( !data ) {
		throw new Exception( "Subtitle not found" )
	}

	def size = data.size() ?: 0
	println size + " found"

  return filterSubtitles( data )
}

def download = { subtitle ->
	print "Downloading subtitle... "
	def filePath = System.getProperty( "java.io.tmpdir" ) + File.separator + subtitle.name + ".zip"

	def output = new FileOutputStream( filePath )
	output.withStream {
		output << new URL( subtitle.link ).openStream()
	}

	println "done"
	return filePath
}

def unzip = { archive ->
	print "Decompressing subtitle... "

	zipFile = new ZipInputStream( new FileInputStream( archive ) )
	zipFile.withStream {
		def entry

		while ( entry = zipFile.nextEntry ) {
			def fileName = entry.name

			if ( fileName =~ /\.srt$/ ) {
				def file = new File( destination, query + ".srt" )
				def output = new FileOutputStream( file )
				output.withStream {
					output << zipFile
				}
			}
		}
	}

	println "done"
}

def logout = {
	print "Disconnecting from OpenSubtitles... "
	server.LogOut( token )
	println "done"
}

// begin
server = new XMLRPCServerProxy( "http://api.opensubtitles.org/xml-rpc" )
try {
	token = login().token

	def subtitle = findSubtitles()
	if ( subtitle ) {
		def zipFile = download( subtitle )
		unzip( zipFile )
	}
}
catch ( e ) {
	println "failed"
	println "Error: " + e
}
finally {
	logout()
}
// end
