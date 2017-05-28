converting
applyAlias: alias
	^ (self class 
		name: alias , name
		state: state
		tickingRate: (tickingRate ifNotNil: [:tr | tr applyAlias: alias])
		body: (body applyAlias: alias))
		token: token