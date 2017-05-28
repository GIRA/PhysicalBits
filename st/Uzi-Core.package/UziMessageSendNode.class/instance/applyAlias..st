converting
applyAlias: alias
	^ (self class
		receiver: (receiver applyAlias: alias)
		selector: selector
		arguments: (arguments collect: [:each | each applyAlias: alias])
		primitive: primitive)
		token: token