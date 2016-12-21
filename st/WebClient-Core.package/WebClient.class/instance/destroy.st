initialize
destroy
	"Destroys the client"

	stream ifNotNil:[
		stream socket ifNotNil:[:s| s destroy].
		stream := nil.
	].
