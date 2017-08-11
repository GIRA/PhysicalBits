instance creation
name: scriptName arguments: argsList body: scriptBody
	^ self name: scriptName 
		arguments: argsList
		state: nil
		tickingRate: nil
		body: scriptBody