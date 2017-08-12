grammar-imports
import
	^ ws , ('import' asParser , ws , identifier , ws , $= asParser , ws , importPath , ws , $; asParser) token , ws
		map: [:ign1 :token :ign2 || alias path ast |
			alias := token parsedValue at: 3.
			path := token parsedValue at: 7.
			(UziImportNode alias: alias path: path) token: token]