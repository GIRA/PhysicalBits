assertions
assertBlock: node nlocals: nlocals
	|  assertBlock assertLocals |
	assertBlock := self assertBlock: node.
	assertBlock ifFalse: [^ false].
	assertLocals := self assert: node locals size = nlocals
		description: ('{1} locals{2} expected' 
			format: { nlocals . nlocals = 0 ifTrue: '' ifFalse: 's' })
		node: node.
	^ assertBlock & assertLocals