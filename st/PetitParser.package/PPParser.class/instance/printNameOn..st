printing
printNameOn: aStream
	self name isNil
		ifTrue: [ aStream print: self hash ]
		ifFalse: [ aStream nextPutAll: self name ]