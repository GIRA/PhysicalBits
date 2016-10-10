button actions
togglePins
	| options |
	options := 2 to: 19.
	(UIManager 
		chooseFrom: (options collect: [:pin | pin asString])
		values: options
		title: 'Toggle pin')
		ifNotNil: [:selection | self togglePinReport: selection]