scripts
script
	^ super script map: [:name :tState :tRate :body :ign |
		UziScriptNode
			name: name
			state: tState
			tickingRate: tRate
			body: body]