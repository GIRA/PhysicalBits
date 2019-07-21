button actions
togglePins
	| activateAll deactivateAll general options toggleAll |
	options := self controller availablePins.
	toggleAll := #'Toggle all'.
	activateAll := #'Activate all'.
	deactivateAll := #'Deactivate all'.
	general := { toggleAll . activateAll . deactivateAll }.
	(UIManager default
		chooseFrom: general, (options 
			collect: [:pin | '{1} ({2})' format: { pin . self getPinReport: pin }])
		values: general, options
		lines: { general size })
		ifNotNil: [:selection | 
			selection caseOf: {
				[toggleAll] -> [options do: [:each | self togglePinReport: each]].
				[activateAll] -> [options do: [:each | self setPin: each report: true]].
				[deactivateAll] -> [options do: [:each | self setPin: each report: false]]
			} otherwise: [self togglePinReport: selection]]