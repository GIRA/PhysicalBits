read/write
readMessage
	"Reads one frame worth of data"

	| byte length |
	byte := stream next ifNil:[
		frameType := 255.
		^nil].
	frameType := byte asInteger.
	frameType < 128 ifTrue:[
		"Frame type < 128: UTF-8 encoded text with 0xFF frame delimiter"
		frameData := (stream upTo: 255 asCharacter) utf8ToSqueak.
	] ifFalse:[
		"Frame type >= 128: Binary data with length prefix"
		length := 0.
		[byte := stream next asInteger.
		length := (length * 128) + (byte bitClear: 128).
		byte >= 128] whileTrue.
		frameData := ByteArray new: length.
		stream nextInto: frameData.
	].
