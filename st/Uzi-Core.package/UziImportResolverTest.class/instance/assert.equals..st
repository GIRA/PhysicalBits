private
assert: expected equals: actual
	^ (expected isKindOf: UziParseNode)
		ifTrue: [[UziParseTreeComparer assert: expected equals: actual]
			on: AssertionFailure
			do: [:ex | self signalFailure: ex description]]
		ifFalse: [super assert: expected equals: actual]