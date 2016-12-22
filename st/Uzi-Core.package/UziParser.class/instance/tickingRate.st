grammar-scripts
tickingRate
	^ number , ws , $/ asParser, ws , ('s' asParser / 'm' asParser / 'h' asParser / 'd' asParser) flatten
		==> [:nodes |
			UziTickingRateNode 
				value: nodes first value
				scale: nodes last asSymbol]