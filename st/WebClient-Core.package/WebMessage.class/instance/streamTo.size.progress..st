streaming
streamTo: dstStream size: size progress: aBlock
	"Stream from the receiver's socket stream to the given destination stream.
	Inbound. Can be used on both request/response depending on
	whether it is utilized by WebClient or WebServer."
	content ifNil:[
		self streamFrom: stream to: dstStream size: size progress: aBlock
	] ifNotNil:[
		self streamFrom: content readStream to: dstStream size: size progress: aBlock
	].