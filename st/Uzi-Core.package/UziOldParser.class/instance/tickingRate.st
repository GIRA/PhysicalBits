grammar-scripts
tickingRate
	^ (number , ws , $/ asParser, ws , ('s' asParser / 'm' asParser / 'h' asParser / 'd' asParser))
	 token map: [:token || nodes |
		nodes := token parsedValue.
		(UziTickingRateNode 
			value: nodes first value
			scale: nodes last asSymbol)
			token: token]