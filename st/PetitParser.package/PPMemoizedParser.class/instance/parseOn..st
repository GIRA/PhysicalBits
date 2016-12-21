parsing
parseOn: aStream
	| memento |
	stream == aStream
		ifFalse: [ self reset: aStream ].
	memento := (buffer at: stream position + 1)
		ifNil: [ buffer at: stream position + 1 put: PPMemento new ].
	memento position isNil
		ifTrue: [
			memento result: (stream size - stream position + 2 < memento count
				ifTrue: [ PPFailure message: 'overflow' at: stream position ]
				ifFalse: [ memento increment. parser parseOn: stream ]).
			memento position: stream position ]
		ifFalse: [ stream position: memento position ].
	^ memento result