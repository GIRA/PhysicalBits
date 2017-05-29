converting
applyAlias: alias
	^ (self class
		left: (left applyAlias: alias)
		right: (right applyAlias: alias))
		token: token