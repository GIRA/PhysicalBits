converting
withState: newState
	^ (self class
		name: name 
		arguments: arguments 
		state: newState 
		tickingRate: tickingRate 
		body: body)
			token: token