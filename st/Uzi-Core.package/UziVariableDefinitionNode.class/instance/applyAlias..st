converting
applyAlias: alias
	^ (self class 
		name: alias, name
		value: value)
		token: token