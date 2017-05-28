converting
applyAlias: alias
	^ (self class 
		value: (value applyAlias: alias)) 
		token: token