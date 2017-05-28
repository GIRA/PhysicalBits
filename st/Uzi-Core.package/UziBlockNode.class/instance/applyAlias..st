converting
applyAlias: alias
	^ (self class
		arguments: (arguments collect: [:each | each applyAlias: alias])
		locals: (locals collect: [:each | each applyAlias: alias])
		statements: (statements collect: [:each | each applyAlias: alias]))
		token: token