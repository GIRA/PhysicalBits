converting
applyAlias: alias
	^ (self class 
		pre: (pre applyAlias: alias)
		condition: (condition applyAlias: alias)
		negated: negated 
		post: (post applyAlias: alias))
		token: token