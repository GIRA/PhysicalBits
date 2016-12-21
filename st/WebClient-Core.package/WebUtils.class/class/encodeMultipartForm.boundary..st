decoding
encodeMultipartForm: fieldMap boundary: boundary
	"Encodes the fieldMap as multipart/form-data.

	The fieldMap may contain MIMEDocument instances to indicate the presence
	of a file to upload to the server. If the MIMEDocument is present, its
	content type and file name will be used for the upload.

	The fieldMap can be EITHER an array of associations OR a Dictionary of 
	key value pairs (the former is useful for providing multiple fields and/or 
	specifying the order of fields)."

	^String streamContents:[:stream|
		(fieldMap as: Dictionary) keysAndValuesDo:[:fieldName :fieldValue | | fieldContent |
			"Write multipart boundary and common headers"
			stream nextPutAll: '--', boundary; crlf.
			stream nextPutAll: 'Content-Disposition: form-data; name="', fieldName, '"'.
			"Figure out if this is a file upload"
			(fieldValue isKindOf: MIMEDocument) ifTrue:[
				stream nextPutAll: ' filename="', fieldValue url pathForFile, '"'; crlf.
				stream nextPutAll: 'Content-Type: ', fieldValue contentType.
				fieldContent := (fieldValue content ifNil:[
					(FileStream readOnlyFileNamed: fieldValue url pathForFile) contentsOfEntireFile.
				]) asString.
			] ifFalse: [fieldContent := fieldValue].
			stream crlf; crlf.
			stream nextPutAll: fieldContent asString.
			stream crlf.
		].
		stream nextPutAll: '--', boundary, '--', String crlf.
	].
