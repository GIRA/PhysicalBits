read/write
readFrameFrom: aStream
	"Reads one frame worth of data"

	| byte length final mask opcode payload |
	byte := (aStream next ifNil:[^nil]) asInteger.

	final := byte anyMask: 128.
	opcode := byte bitAnd: 15.

	byte := (aStream next ifNil:[^nil]) asInteger.
	mask := byte anyMask: 128.
	length := byte bitAnd: 127.

	length = 126 ifTrue:[
		length := self nextNumber: 2 from: aStream.
	] ifFalse:[
		length = 127 ifTrue:[length := self nextNumber: 8 from: aStream].
	].

	mask 
		ifTrue:[mask := aStream next: 4] 
		ifFalse:[mask := nil].

	payload := ByteArray new: length.
	aStream next: length into: payload.

	"Unmask payload"
	mask ifNotNil:[self xor: payload with: mask].

	"First see if this is a control frame which can be inserted into fragmented messages."
	opcode >= 8 ifTrue:[^self handleControl: payload type: opcode].

	"Now see if this is a fragmented message"
	frameType ifNil:[
		frameType := opcode.
		frameData := Array with: payload.
	] ifNotNil:[
		frameData := frameData copyWith: payload.
	].

	final ifTrue:[
		"This was the final fragment"
		frameData size = 1 ifTrue:[
			payload := frameData first
		] ifFalse:[
			payload := frameData allButFirst 
							inject: frameData first 
							into:[:each :next| each, next].
		].
		frameType = 1 ifTrue:[payload := payload asString utf8ToSqueak].
		self handleMessage: payload type: frameType.
		frameType := frameData := nil.
	].
