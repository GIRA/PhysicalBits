grammar-imports
import
	^ ws , 'import' asParser , ws , identifier , ws , $= asParser , ws , importPath token, #newline asParser
		==> [:nodes || alias path ast |
			alias := nodes at: 4.
			path := nodes at: 8.
			"Return an association with the path token as key. This way if the parsing failed we have the
			token and we can return a PPFailure with the error position. However, we can't return a PPFailure 
			here because then the import would be ignored."
			path -> ((self class new librariesDirectory: libraries) 
				parseFile: path parsedValue
				withAlias: alias, '@')]