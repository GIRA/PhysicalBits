grammar-calls
namedArg
	^ (identifier , $: asParser) optional , expression
		map: [:key :value | (key ifNotNil: [:k | k first]) -> value]