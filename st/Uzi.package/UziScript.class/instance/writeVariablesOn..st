as yet unclassified
writeVariablesOn: aStream
	aStream nextPut: variables size.
	variables ifEmpty: [^ self].
	self halt