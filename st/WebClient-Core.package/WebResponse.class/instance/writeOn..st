printing
writeOn: aStream
	"Store this response on aStream"

	self writeStatusOn: aStream.
	super writeOn: aStream.
