connection
connectOn: port
	controller connectOn: port.	
	pinTexts keysDo: [:pin | self setPin: pin report: true]