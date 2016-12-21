fields
multipartFieldsDo: aBlock
	"Evaluate aBlock with the multipart fields of a post request.
	The block is expected to take three arguments:
		* The headers from the part being processed
		* The parameters from the content-disposition header
		* The content from the part being processed
	"

	^(self isPostRequest and: [
		self contentType beginsWith: 'multipart/form-data'])
			ifTrue: [WebUtils decodeMultipartForm: self content readStream boundary: self multipartBoundary do: aBlock]
			ifFalse: [Dictionary new]