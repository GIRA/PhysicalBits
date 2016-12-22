grammar-literals
float
	^ (integer, $. asParser, digits) flatten map: [:val | Float readFrom: val]