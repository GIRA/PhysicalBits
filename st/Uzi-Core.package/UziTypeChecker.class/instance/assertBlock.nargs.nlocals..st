assertions
assertBlock: node nargs: nargs nlocals: nlocals
	| assertArgs assertBlock assertLocals |
	assertBlock := self assertBlock: node.
	assertBlock ifFalse: [^ false].
	assertArgs := self assert: node arguments size = nargs
		description: ('{1} argument{2} expected' 
			format: { nargs . nargs = 0 ifTrue: '' ifFalse: 's' })
		node: node.
	assertLocals := self assert: node locals size = nlocals
		description: ('{1} locals{2} expected' 
			format: { nlocals . nlocals = 0 ifTrue: '' ifFalse: 's' })
		node: node.
	^ assertBlock & assertArgs & assertLocals