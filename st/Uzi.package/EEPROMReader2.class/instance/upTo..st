accessing
upTo: value
	^ Array streamContents: [:stream || keepGoing next |
		keepGoing := true.
		[keepGoing] whileTrue: [
			next := self next.
			next = value 
				ifTrue: [keepGoing := false]
				ifFalse: [stream nextPut: next]]]