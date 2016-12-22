grammar-unary
unaryMessage
	^ ws , unarySelector , $: asParser not
		map: [:ign1 :sel :ign2 | self message: sel args: #()]