grammar-scripts
scriptName
	^ $@ asParser, identifier map: [:ign :name | name asSymbol]