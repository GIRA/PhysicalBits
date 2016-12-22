grammar-identifier
identifier
	^ (#letter asParser , #word asParser star) flatten