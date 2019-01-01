accessing
scriptNamed: aString ifAbsent: aBlock
	^ self scripts 
		detect: [:script | script scriptName = aString]
		ifNone: aBlock