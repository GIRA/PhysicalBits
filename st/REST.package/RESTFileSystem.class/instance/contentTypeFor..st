requests
contentTypeFor: file
	| extension |
	extension := (file name subStrings: '.') last.
	^ extension caseOf: {
		['html'] -> ['text/html; charset=utf-8'].
		['js'] -> ['text/javascript; charset=utf-8'].
		['css'] -> ['text/css; charset=utf-8']
	} otherwise: [file mimeTypes
		ifNil: ['application/octet-stream']
		ifNotNil: [:types | types first]]