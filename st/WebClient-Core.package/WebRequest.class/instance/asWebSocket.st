converting
asWebSocket
	"Convert this request into a WebSocket"

	"Figure out the version of the protocol"
	| field version |
	version := self headerAt: 'Sec-WebSocket-Version' ifAbsent:[nil].
	version ifNil:[
		field := self headerAt: 'Sec-WebSocket-Key1' ifAbsent:[nil].
		field 
			ifNil:[^self asWebSocket68] 
			ifNotNil:[^self asWebSocket00].
	].

	"Attempt WebSocket07 conversion"
	^self asWebSocket07
