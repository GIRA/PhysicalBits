button actions
toggleGlobals
	| options |
	options := self globals.
	(UIManager 
		chooseFrom: options
		values: options
		title: 'Toggle global')
		ifNotNil: [:selection |	self toggleGlobalReport: selection]