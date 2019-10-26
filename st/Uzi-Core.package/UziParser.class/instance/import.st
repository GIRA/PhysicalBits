grammar-imports
import
	^ ws , 
	('import' asParser , 
	ws , 
	(identifier , 	ws , 'from' asParser) optional , 
	ws , 
	importPath , 
	ws , 
	($; asParser / block)) 
		token , 
	ws
		map: [:ign1 :token :ign2 || alias path initBlock |
			alias := (token parsedValue at: 3) ifNotNil: [:e | e first].
			path := token parsedValue at: 5.
			initBlock := token parsedValue at: 7.
			initBlock = $; ifTrue: [initBlock := nil].
			(UziImportNode alias: alias path: path initializationBlock: initBlock) 
				token: token]