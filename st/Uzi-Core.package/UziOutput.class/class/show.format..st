writing
show: aString format: anArray
	| msg |
	msg := aString format: anArray.
	buffer nextPutAll: msg