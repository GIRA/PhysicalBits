button actions
toggleGlobals
	self globals ifNotEmpty: [:options |
		| toggleAll activateAll deactivateAll general |
		toggleAll := #'Toggle all'.
		activateAll := #'Activate all'.
		deactivateAll := #'Deactivate all'.
		general := { toggleAll . activateAll . deactivateAll }.
		(UIManager default
			chooseFrom: general, (options 
				collect: [:opt | '{1} ({2})' format: { opt . self getGlobalReport: opt }])
			values: general, options
			lines: {general size})
			ifNotNil: [:selection |
				selection caseOf: {
					[toggleAll] -> [options do: [:each | self toggleGlobalReport: each]].
					[activateAll] -> [options do: [:each | self setGlobal: each report: true]].
					[deactivateAll] -> [options do: [:each | self setGlobal: each report: false]]
				} otherwise: [self toggleGlobalReport: selection]]]