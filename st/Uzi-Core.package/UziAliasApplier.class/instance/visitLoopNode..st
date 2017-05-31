visiting
visitLoopNode: node
	^ (node class 
		pre: (self visit: node pre)
		condition: (self visit: node condition)
		negated: node negated 
		post: (self visit: node post))
		token: node token