instance creation
name: scriptName arguments: argsList state: scriptState tickingRate: tickingRate body: scriptBody
	^ self basicNew 
		initializeWithName: scriptName 
		arguments: argsList
		state: scriptState 
		tickingRate: tickingRate 
		body: scriptBody