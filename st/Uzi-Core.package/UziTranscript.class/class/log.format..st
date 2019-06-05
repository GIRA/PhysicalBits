writing
log: aString format: anArray
	| msg |
	msg := aString format: anArray.
	buffer nextPutAll: msg; cr