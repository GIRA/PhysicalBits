initialize
close
	"Close the client's stream"

	stream ifNotNil:[
		stream close.
		stream := nil.
	].
