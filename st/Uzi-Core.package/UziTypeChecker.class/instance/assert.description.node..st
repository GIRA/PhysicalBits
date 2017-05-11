assertions
assert: aBoolean description: aString node: node
	aBoolean ifFalse: [errors add: (UziTypeError on: node description: aString)].
	^ aBoolean