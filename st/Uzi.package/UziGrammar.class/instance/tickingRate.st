grammar-scripts
tickingRate
	^ numberToken , $/ asParser, ('s' asParser / 'm' asParser / 'h' asParser / 'd' asParser) uziToken