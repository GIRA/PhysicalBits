installing
installCurrentProgram
	^ currentProgram 
		ifNil: [ByteArray new: 0]
		ifNotNil: [:p | self install: p]