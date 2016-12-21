private
operator: aSymbol parser: aParser do: aBlock
	parser isNil
		ifTrue: [ ^ self error: 'You did not specify a term when creating the receiver.' ].
	operators isNil
		ifTrue: [ ^ self error: 'Use #group: to define precedence groups in descending order.' ].
	(operators at: aSymbol ifAbsentPut: [ OrderedCollection new ])
		addLast: (Array with: aParser asParser with: aBlock)