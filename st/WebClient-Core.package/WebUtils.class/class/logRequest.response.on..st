misc
logRequest: request response: response on: streamOrFilename
	"Log a request in common log format on the given stream / file."

	| entry stream |

	"Create the log entry for the request/response pair"
	entry := self logEntryFor: request response: response.

	"If the argument is a string, it represents the file name to log to"
	streamOrFilename isString ifTrue:[
		stream := [FileStream oldFileNamed: streamOrFilename] 
			on: FileDoesNotExistException
			do:[FileStream newFileNamed: streamOrFilename].
		stream wantsLineEndConversion: true; setToEnd.
	] ifFalse:[stream := streamOrFilename].

	stream nextPutAll: entry; cr.

	stream == streamOrFilename 
		ifTrue:[stream flush]
		ifFalse:[stream close].
