grammar
identifier
	^ ((#letter asParser , #word asParser star) separatedBy: $. asParser) flatten