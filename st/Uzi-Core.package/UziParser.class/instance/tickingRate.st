scripts
tickingRate
	^ super tickingRate map: [:n :ign :scale |
		UziTickingRateNode 
			value: n inputValue asNumber
			scale: scale inputValue asSymbol]