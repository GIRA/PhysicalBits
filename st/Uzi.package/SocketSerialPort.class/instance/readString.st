as yet unclassified
readString
	socket ifNil: [^ ''].
	^ socket receiveAvailableData 