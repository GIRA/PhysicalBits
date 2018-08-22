visiting
visitLoopNode: node
	self saveIntervalFor: node
		during: [
			node pre isEmpty
				ifTrue: [^ node negated 
					ifTrue: [self writeUntil: node]
					ifFalse: [self writeWhile: node]].
			node post isEmpty 
				ifTrue: [^ node negated 
					ifTrue: [self writeDoUntil: node]
					ifFalse: [self writeDoWhile: node]].
			self error: 'Invalid loop node']