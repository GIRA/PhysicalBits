grammar
identifier
	^ ((($_ asParser / #letter asParser) , ($_ asParser / #word asParser) star) 
		separatedBy: $. asParser) flatten