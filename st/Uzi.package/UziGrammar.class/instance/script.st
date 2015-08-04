grammar-scripts
script
	^ scriptName, tickingState, tickingRate, scriptBody, $. asParser uziToken star 