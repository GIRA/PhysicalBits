building
script	
	bytecodes withIndexDo: [:instruction :index |
		instruction updateLabelArgument: (self labelsFrom: index)].
	^ UziScript
		name: scriptName
		ticking: ticking
		delay: delay
		bytecodes: bytecodes 