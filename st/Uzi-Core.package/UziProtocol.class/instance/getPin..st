accessing-pins
getPin: pinNumber
	^ self pins 
		at: pinNumber 
		ifAbsentPut: [UziPin number: pinNumber]