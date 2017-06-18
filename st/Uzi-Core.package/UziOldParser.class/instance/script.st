grammar-scripts
script
	^ ws , (scriptName, ws , tickingState optional, ws , tickingRate optional, ws , block) token , separator optional
	map: [:ign1 :token :ign2 || nodes |
		nodes := token parsedValue.
		(UziScriptNode
			name: nodes first
			arguments: nodes seventh key
			state: nodes third
			tickingRate: nodes fifth
			body: nodes seventh value)
			token: token]