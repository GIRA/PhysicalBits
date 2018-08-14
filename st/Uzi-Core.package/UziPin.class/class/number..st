instance creation
number: anInteger
	| pinName |
	pinName := self names at: anInteger ifAbsent: [anInteger asString].
	^ self name: pinName number: anInteger