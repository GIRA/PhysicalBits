scripts
script
	^ super script map: [:scriptName :tickingState :tickingRate :body |
		UziScriptNode
			name: scriptName
			state: tickingState
			tickingRate: tickingRate
			body: body]