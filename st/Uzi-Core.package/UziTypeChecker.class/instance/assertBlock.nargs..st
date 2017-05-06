assertions
assertBlock: node nargs: nargs
	self assertBlock: node.
	self assert: node arguments size = nargs
		description: ('{1} argument{2} expected' 
			format: { nargs . nargs = 0 ifTrue: '' ifFalse: 's' })
		node: node