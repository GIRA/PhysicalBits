instance creation
number: anInteger idleTimeout: idleTimeout
	| pinName |
	pinName := self names at: anInteger ifAbsent: [anInteger asString].
	^ self name: pinName number: anInteger idleTimeout: idleTimeout