tests
test038Statements
	| pp |
	pp := parser statement end.
	self deny: (pp parse: ' a = 4;  ') isPetitFailure.
	self deny: (pp parse: '  return 0.4  ;') isPetitFailure.	
	self deny: (pp parse: '  if read(D13) {} ') isPetitFailure.
	self deny: (pp parse: '  while 0.4 {} ') isPetitFailure.
	self deny: (pp parse: '  do{} while 0.4; ') isPetitFailure.
	self deny: (pp parse: '  read(D13);  ') isPetitFailure.
	self deny: (pp parse: '  0.4;  ') isPetitFailure