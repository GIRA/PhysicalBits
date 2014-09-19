scripts
tickingRate
	^ super tickingRate map: [:number :ign :scale |
		UziTickingRateNode 
			value: number inputValue asNumber
			scale: scale inputValue asSymbol]