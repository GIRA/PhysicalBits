scripts
script
	^ super script map: [:scriptName :tickingState :tickingRate :body :ign |
		UziScriptNode
			name: scriptName
			state: tickingState
			tickingRate: tickingRate
			body: body]