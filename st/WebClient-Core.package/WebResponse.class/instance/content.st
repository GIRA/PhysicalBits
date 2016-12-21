accessing
content
	"Reimplemented to close the socket if the request is transient"

	content ifNil:[
		content := self getContent.
		self closeIfTransient.
	].
	^content