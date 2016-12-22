grammar-scripts
script
	^ ws , scriptName, ws , tickingState optional, ws , tickingRate optional, ws , block , separator optional
		==> [:nodes | UziScriptNode
			name: nodes second
			state: nodes fourth
			tickingRate: nodes sixth
			body: nodes eighth statements]