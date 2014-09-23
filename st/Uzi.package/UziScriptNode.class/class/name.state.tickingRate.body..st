instance creation
name: scriptName state: scriptState tickingRate: tickingRate body: scriptBody
	^ self basicNew 
		initializeWithName: scriptName 
		state: scriptState 
		tickingRate: tickingRate 
		body: scriptBody