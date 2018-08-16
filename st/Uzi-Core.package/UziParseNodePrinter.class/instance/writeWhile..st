visiting
writeWhile: node
	self write: 'while ';
		visit: node condition.
	node body isEmpty
		ifTrue: [self write: ';']
		ifFalse: [self write: ' ';
			visit: node body]