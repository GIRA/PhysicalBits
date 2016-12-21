read/write
send: data code: opcode final: isFinal mask: doMask
	"Sends unfragmented data using the given opcode"

	| byte1 byte2 mask xLength copy |
	byte1 := opcode. "FIN = 1, RSV1=RSV2=RSV3=0"
	isFinal ifTrue:[byte1 := byte1 bitOr: 128].
	data size > 125 ifTrue:[
		data size > 16rFFFF ifTrue:[
			byte2 := 127.
			xLength := ByteArray new: 8.
			(WriteStream on: xLength) nextNumber: 8 put: data size.
		] ifFalse:[
			byte2 := 126.
			xLength := ByteArray new: 2.
			(WriteStream on: xLength) nextNumber: 2 put: data size.
		].
	] ifFalse:[
		byte2 := data size.
		xLength := #[].
	].
	doMask ifTrue:[
		byte2 := byte2 bitOr: 128. "Mask = 1"
		mask := ByteArray new: 4.
		1 to: 4 do:[:i| mask at: i put: 256 atRandom-1].
		copy := data copy.
		self xor: copy with: mask.
	] ifFalse:[
		copy := data.
	].
	stream nextPut: byte1.
	stream nextPut: byte2.
	stream nextPutAll: xLength.
	mask ifNotNil:[stream nextPutAll: mask].
	stream nextPutAll: copy.
	stream flush.