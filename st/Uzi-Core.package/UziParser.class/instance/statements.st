grammar-statements
statements 
	^ ws , statement optional , (separator , statement map: [:ign :stmt | stmt]) star , separator optional
		map: [:ign1 :first :rest :ign2 | { first } , rest select: [:each | each notNil]]