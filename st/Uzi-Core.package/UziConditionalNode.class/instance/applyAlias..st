converting
applyAlias: alias
	^ (self class
		condition: (condition applyAlias: alias)
		true: (trueBranch applyAlias: alias)
		false: (falseBranch applyAlias: alias))
		token: token