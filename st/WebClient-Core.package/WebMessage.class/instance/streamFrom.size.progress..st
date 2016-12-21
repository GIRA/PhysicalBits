streaming
streamFrom: srcStream size: size progress: aBlock
	"Stream the given source stream to the message's socket stream.
	Outbound. Can be used on both request/response depending on
	whether it is utilized by WebClient or WebServer."

	^self streamFrom: srcStream to: stream size: size progress: aBlock