visiting
visitCallNode: node
	node isPrimitive
		ifFalse: [| script |
			script := self program scripts
				detect: [:s | s name = node selector]
				ifNone: [nil].
			script ifNotNil: [:s | self visit: s]].
	self visitChildren: node