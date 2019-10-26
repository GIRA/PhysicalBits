grammar-primitives
primitive
	^ ws , 
	('prim' asParser , ws , 
	((binarySelector / identifier), ws , $: asParser) token optional , ws ,
	identifier , ws ,
	$; asParser) token, 
	ws
		map: [:ign1 :token :ign2 || alias name |
			name := token parsedValue at: 5.
			alias := (token parsedValue at: 3) 
				ifNil: [name]
				ifNotNil: [:aliasToken | aliasToken parsedValue first].
			(UziPrimitiveDeclarationNode alias: alias name: name)
				token: token]