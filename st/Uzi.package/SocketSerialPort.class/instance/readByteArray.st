as yet unclassified
readByteArray
	socket ifNil: [^ #() asByteArray].
	^ socket receiveAvailableData asByteArray