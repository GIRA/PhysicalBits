grammar-imports
importPath
	^ $' asParser ,  $' asParser negate star flatten , $' asParser
		map: [:ign1 :path :ign2 | path]