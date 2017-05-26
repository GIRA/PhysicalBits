grammar-imports
import
	^ ws , 'import' asParser , ws , identifier , ws , $= asParser , ws , importPath , #newline asParser
		==> [:nodes | 
			UziImportNode
				name: (nodes at: 4)
				path: (nodes at: 8)]