examples
browseRequest: request
	"Handle an HTTP request for browsing some resource"

	| path fd entry file |

	"Extract the file path from the request"
	path := request url findTokens: '/'.
	path ifEmpty:[^self browseDir: FileDirectory default request: request.].

	"Find the directory entry for the resource"
	fd := path allButLast inject: FileDirectory default into:[:dir :part| dir directoryNamed: part].
	entry := fd entryAt: path last ifAbsent:[^request send404Response].

	"Reply with the proper resource"
	entry isDirectory ifTrue:[
		"Send file listing. We handle this synchronously for simplicity."
		self browseDir: (fd directoryNamed: entry name) request: request.
	] ifFalse:[
		"Send file content. Since files can be large, we fork this off.
		However, since we forked it, we need to handle two different
		possible conditions:
			- closing the file in case of error
			- handling errors when sending it
		This makes the code below a bit ugly"
		[[
			file := fd readOnlyFileNamed: entry name.
			[self browseFile: file request: request.] 
				ensure:[file close] 	"close file even in case of error"
		] on: Error do:[]				"ignore errors altogether"
		] fork. 						"fork it"
	].
