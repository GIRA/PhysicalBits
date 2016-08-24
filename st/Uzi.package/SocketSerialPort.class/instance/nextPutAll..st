as yet unclassified
nextPutAll: data
	socket ifNil: [^ -1].
	^ socket sendData: data