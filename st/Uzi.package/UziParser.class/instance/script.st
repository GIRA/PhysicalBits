scripts
script
	^ super script map: [:name :tState :tRate :body :ign |
		UziScriptNode
			name: scriptName
			state: tickingState
			tickingRate: tickingRate
			body: body]