button actions
toggleGlobals
	self globals ifNotEmpty: [:options |
		(UIManager default
			chooseFrom: options
			values: options
			title: 'Toggle global')
			ifNotNil: [:selection |	self toggleGlobalReport: selection]]