grammar-imports
import
	^ ws , 'import' asParser , ws , identifier , ws , $= asParser , ws , importPath , #newline asParser
		==> [:nodes || alias path ast |
			alias := nodes at: 4.
			path := nodes at: 8.
			(self class new librariesDirectory: libraries) 
				parseFile: path
				withAlias: alias, '@']